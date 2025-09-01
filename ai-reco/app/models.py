import os
import threading
from sentence_transformers import SentenceTransformer

_MODEL = None
_LOCK = threading.Lock()

def get_encoder():
    global _MODEL
    if _MODEL is not None:
        return _MODEL
    with _LOCK:
        if _MODEL is None:
            model_name = os.getenv("EMBED_MODEL", "BAAI/bge-m3")
            device = os.getenv("EMBED_DEVICE", None)  # "cuda" | "cpu" | None (auto)
            _MODEL = SentenceTransformer(model_name, device=device)
    return _MODEL
