import os, threading
from typing import List, Dict, Any, Tuple
import numpy as np
from sentence_transformers import SentenceTransformer
import faiss

_EMBED_MODEL = os.getenv("EMBED_MODEL", "BAAI/bge-m3")
_DEVICE = os.getenv("EMBED_DEVICE", "cpu")

_model_lock = threading.Lock()
_index_lock = threading.Lock()
_model = None
_index = None
_corpus: List[Dict[str, Any]] = []

def _get_model():
    global _model
    with _model_lock:
        if _model is None:
            _model = SentenceTransformer(_EMBED_MODEL, device=_DEVICE)
        return _model

def _embed(texts: List[str]) -> np.ndarray:
    mdl = _get_model()
    embs = mdl.encode(texts, normalize_embeddings=True, batch_size=64, convert_to_numpy=True)
    return embs.astype("float32")

def reindex(items: List[Dict[str, Any]]) -> int:
    """items: [{id, title, text, group?}]"""
    global _index, _corpus
    _corpus = items[:]
    if not _corpus:
        with _index_lock: _index = None
        return 0
    vecs = _embed([(it.get("title","")+" "+it.get("text","")).strip() for it in _corpus])
    d = vecs.shape[1]
    idx = faiss.IndexFlatIP(d)  # cosine (vì embedding normalized)
    idx.add(vecs)
    with _index_lock:
        _index = idx
    return len(_corpus)

def search(query: str, k: int = 5) -> List[Tuple[float, Dict[str, Any]]]:
    if not query or _index is None or not _corpus:
        return []
    q = _embed([query])
    with _index_lock:
        D, I = _index.search(q, min(k, len(_corpus)))
    out = []
    for score, i in zip(D[0].tolist(), I[0].tolist()):
        if i == -1: continue
        out.append((float(score), _corpus[i]))
    return out

def rank(query: str, candidates: List[Dict[str, Any]], k: int = 10) -> List[Dict[str, Any]]:
    if not candidates:
        return []
    texts = [(c.get("title","")+" "+c.get("text","")).strip() for c in candidates]
    cand_emb = _embed(texts)
    q_emb = _embed([query])
    scores = (q_emb @ cand_emb.T)[0].tolist()
    ranked = sorted(
        [{**c, "score": float(s)} for c, s in zip(candidates, scores)],
        key=lambda x: x["score"], reverse=True
    )
    return ranked[:k]

