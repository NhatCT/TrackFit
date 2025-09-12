import os, numpy as np
from typing import List, Tuple
from .models import get_encoder
from .schemas import AiRankRequest, AiRankedExercise
from .utils import normalize_intensity, map_difficulty, build_reason

def encode_texts(texts: List[str]) -> np.ndarray:
    model = get_encoder()
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
    sims = cand_embs @ q_emb  # cosine

    minutes_pref = (req.context or {}).get("availableMinutes") or 25
    intensity_pref = normalize_intensity((req.context or {}).get("intensity"))
    recent_ids = set((req.context or {}).get("recentIds") or [])

    bonuses = np.zeros_like(sims, dtype=np.float32)
    for i, c in enumerate(req.candidates):
        if c.minutes:
            diff = abs(int(c.minutes) - int(minutes_pref))
            m_bonus = max(0.0, 1.0 - (diff / 60.0))
            bonuses[i] += 0.25 * m_bonus
        if c.exerciseId not in recent_ids:
            bonuses[i] += 0.25
        d_norm = map_difficulty(c.difficulty)
        if (intensity_pref == d_norm):
            bonuses[i] += 0.25
        elif (intensity_pref, d_norm) in {("High","Medium"),("Medium","Low")}:
            bonuses[i] += 0.10

    w1 = float(os.getenv("W_SEMANTIC", 0.85))
    w2 = 1.0 - w1

    s_min, s_max = float(sims.min()), float(sims.max())
    sims01 = (sims - s_min) / (s_max - s_min) if s_max > s_min else np.ones_like(sims) * 0.5
    b_min, b_max = float(bonuses.min()), float(bonuses.max())
    bonus01 = (bonuses - b_min) / (b_max - b_min) if b_max > b_min else np.zeros_like(bonuses)

    scores = w1 * sims01 + w2 * bonus01

    sc_min, sc_max = float(scores.min()), float(scores.max())
    norm = (scores - sc_min) / (sc_max - sc_min) if sc_max > sc_min else np.ones_like(scores) * 0.5

    goal = (req.user.goalType if req.user else None) or (req.context or {}).get("goalType")
    reasons = [
        build_reason(goal, intensity_pref, req.candidates[i].minutes,
                     (1 if req.candidates[i].exerciseId not in recent_ids else 0))
        for i in range(len(req.candidates))
    ]

    ranked: List[Tuple[int, float, str]] = [
        (req.candidates[i].exerciseId, float(norm[i]), reasons[i])
        for i in range(len(req.candidates))
    ]
    ranked.sort(key=lambda t: t[1], reverse=True)
    return [AiRankedExercise(exerciseId=eid, score=sc, reason=rs) for eid, sc, rs in ranked]
