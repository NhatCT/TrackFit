import os, traceback
from typing import List
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from .schemas import AiRankRequest, AiRankedExercise, EmbedRequest
from .ranker import rank, encode_texts
from .embed_index import INDEX
from .models import get_encoder

app = FastAPI(title="AI Reco Service", version="1.2.0")

origins = [
    os.getenv("CORS_ORIGIN_REACT", "http://localhost:3000"),
    os.getenv("CORS_ORIGIN_SPRING", "http://localhost:8080"),
]
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.on_event("startup")
def _warmup():
    # preload encoder để nếu lỗi model/token/mạng thì thấy ngay lúc start
    _ = get_encoder()

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/rank", response_model=List[AiRankedExercise])
def rank_endpoint(req: AiRankRequest):
    try:
        return rank(req)
    except Exception as e:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/embed")
def embed_endpoint(req: EmbedRequest):
    try:
        embs = encode_texts(req.texts)
        return {"vectors": embs.tolist()}
    except Exception as e:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/rag")
def rag_query(q: str, topk: int = 5):
    return INDEX.query(q, topk=topk)

# endpoint chẩn đoán nhanh (tuỳ chọn)
@app.get("/__version__")
def version():
    import numpy, sentence_transformers, transformers
    try:
        import torch
        torch_v = torch.__version__
    except Exception:
        torch_v = "not-installed"
    try:
        import faiss
        faiss_v = getattr(faiss, "__version__", "unknown")
    except Exception:
        faiss_v = "not-installed"
    return {
        "python": os.sys.version,
        "torch": torch_v,
        "numpy": numpy.__version__,
        "sentencetransformers": sentence_transformers.__version__,
        "transformers": transformers.__version__,
        "faiss": faiss_v,
        "embed_model": os.getenv("EMBED_MODEL"),
    }
