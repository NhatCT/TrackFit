import os
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .schemas import AiRankRequest, AiRankedExercise, EmbedRequest
from .ranker import rank, encode_texts

app = FastAPI(title="AI Reco Service", version="1.1.0")

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

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/rank", response_model=list[AiRankedExercise])
def rank_endpoint(req: AiRankRequest):
    return rank(req)

@app.post("/embed")
def embed_endpoint(req: EmbedRequest):
    embs = encode_texts(req.texts)
    return {"vectors": embs.tolist()}
