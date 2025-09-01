import os
import math
import numpy as np
from typing import List, Tuple
from .models import get_encoder
from .schemas import AiRankRequest, AiRankedExercise
from .utils import normalize_intensity, map_difficulty, build_reason

def encode_texts(texts: List[str]) -> np.ndarray:
    model = get_encoder()
    # normalize_embeddings=True => cosine-ready
    return model.encode(texts, normalize_embeddings=True, convert_to_numpy=True)

def candidate_to_text(c):
    bits = [
        c.name or "",
        f"muscle: {c.muscleGroup or ''}",
        f"minutes: {c.minutes or ''}",
        f"difficulty: {c.difficulty or ''}",
    ]
    return " | ".join(str(b) for b in bits if b)

def context_to_query(req: AiRankRequest) -> str:
    goal = (req.user.goalType if req.user else None) or (req.context or {}).get("goalType")
    intensity = normalize_intensity((req.context or {}).get("intensity"))
    minutes = (req.context or {}).get("availableMinutes")
    q = f"Goal: {goal or 'general'}; Intensity: {intensity}; Time: {minutes or 25} minutes workout; prefer variety"
    kw = (req.context or {}).get("kw")
    if kw:
        q += f"; query: {kw}"
    return q

def rank(req: AiRankRequest) -> List[AiRankedExercise]:
    if not req.candidates:
        return []

    cand_texts = [candidate_to_text(c) for c in req.candidates]
    cand_embs = encode_texts(cand_texts)

    query = context_to_query(req)
    q_emb = encode_texts([query])[0]

    # cosine sim (vì đã normalize)
    sims = cand_embs @ q_emb  # [-1, 1]

    minutes_pref = (req.context or {}).get("availableMinutes") or 25
    intensity_pref = normalize_intensity((req.context or {}).get("intensity"))
    # recentIds truyền từ client/context để thay thế global-state
    recent_ids = set((req.context or {}).get("recentIds") or [])

    # --- bonus tái cân bằng ---
    # scale bonus vào [0,1] rồi nội suy theo w2
    bonuses = np.zeros_like(sims, dtype=np.float32)

    for i, c in enumerate(req.candidates):
        # phút gần với preferences
        if c.minutes:
            diff = abs(int(c.minutes) - int(minutes_pref))
            # phút càng sát càng tốt, hết hiệu lực nếu lệch > 60'
            m_bonus = max(0.0, 1.0 - (diff / 60.0))
            bonuses[i] += 0.25 * m_bonus  # tối đa +0.25

        # novelty
        n_bonus = 0.25 if c.exerciseId not in recent_ids else 0.0
        bonuses[i] += n_bonus

        # difficulty khớp intensity
        d_norm = map_difficulty(c.difficulty)
        if (intensity_pref == "Low" and d_norm == "Low") or \
           (intensity_pref == "Medium" and d_norm == "Medium") or \
           (intensity_pref == "High" and d_norm == "High"):
            bonuses[i] += 0.25  # khớp hoàn toàn
        elif (intensity_pref, d_norm) in {("High","Medium"),("Medium","Low")}:
            bonuses[i] += 0.10  # khớp tương đối

    # trọng số
    w1 = float(os.getenv("W_SEMANTIC", 0.85))  # giảm nhẹ để bonus “nghe thấy”
    w1 = min(max(w1, 0.0), 1.0)
    w2 = 1.0 - w1

    # Chuẩn hoá sims về [0,1] để cộng với bonus cùng miền
    s_min, s_max = float(sims.min()), float(sims.max())
    if s_max - s_min < 1e-9:
        sims01 = np.ones_like(sims) * 0.5
    else:
        sims01 = (sims - s_min) / (s_max - s_min)

    # bonuses hiện đã nằm ~[0,0.75]; ép về [0,1]
    b_min, b_max = float(bonuses.min()), float(bonuses.max())
    if b_max - b_min < 1e-9:
        bonus01 = np.zeros_like(bonuses)
    else:
        bonus01 = (bonuses - b_min) / (b_max - b_min)

    scores = w1 * sims01 + w2 * bonus01

    # Chuẩn hoá cuối
    sc_min, sc_max = float(scores.min()), float(scores.max())
    if sc_max - sc_min < 1e-9:
        norm = np.ones_like(scores) * 0.5
    else:
        norm = (scores - sc_min) / (sc_max - sc_min)

    goal = (req.user.goalType if req.user else None) or (req.context or {}).get("goalType")
    reasons = [
        build_reason(
            goal=goal,
            intensity=intensity_pref,
            minutes=req.candidates[i].minutes,
            novelty=(1 if req.candidates[i].exerciseId not in recent_ids else 0),
        )
        for i in range(len(req.candidates))
    ]

    ranked: List[Tuple[int, float, str]] = [
        (req.candidates[i].exerciseId, float(norm[i]), reasons[i]) for i in range(len(req.candidates))
    ]
    ranked.sort(key=lambda t: t[1], reverse=True)

    return [AiRankedExercise(exerciseId=eid, score=sc, reason=rs) for eid, sc, rs in ranked]
