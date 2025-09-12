import os, threading
from sentence_transformers import SentenceTransformer

_MODEL = None
_LOCK = threading.Lock()

def get_encoder():
    global _MODEL
    if _MODEL:
        return _MODEL
    with _LOCK:
        if _MODEL is None:
            model_name = os.getenv("EMBED_MODEL", "BAAI/bge-m3")
            device = os.getenv("EMBED_DEVICE", None)  # "cuda" | "cpu" | None
            _MODEL = SentenceTransformer(model_name, device=device)
    return _MODEL
