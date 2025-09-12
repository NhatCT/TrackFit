import faiss
import numpy as np
from typing import List, Dict, Any, Optional, Iterable
from sentence_transformers import SentenceTransformer
from .models import get_encoder

class MiniRAGIndex:
    def __init__(self, encoder: Optional[SentenceTransformer] = None,
                 model_name: str = "sentence-transformers/all-MiniLM-L6-v2"):
        self.model = encoder or SentenceTransformer(model_name)
        self.dim = self.model.get_sentence_embedding_dimension()
        self.index = faiss.IndexFlatIP(self.dim)  # cosine (vì đã normalize)
        self.corpus: List[Dict[str, Any]] = []

    def _emb(self, texts: Iterable[str]) -> np.ndarray:
        em = self.model.encode(
            list(texts),
            normalize_embeddings=True,
            convert_to_numpy=True,
            show_progress_bar=False,
        )
        return np.ascontiguousarray(em.astype("float32"))

    def add_docs(self, docs: List[Dict[str, Any]]) -> None:
        if not docs:
            return
        vecs = self._emb(d["text"] for d in docs)
        if vecs.shape[1] != self.dim:
            raise ValueError(f"Embedding dim mismatch: index={self.dim}, new={vecs.shape[1]}")
        self.index.add(vecs)
        self.corpus.extend(docs)

    def query(self, q: str, topk: int = 8):
        if self.index.ntotal == 0:
            return []
        k = int(min(max(topk, 1), self.index.ntotal))
        qv = self._emb([q])
        D, I = self.index.search(qv, k)
        out = []
        for score, idx in zip(D[0], I[0]):
            if 0 <= idx < len(self.corpus):
                d = self.corpus[idx]
                out.append({**d, "score": float(score)})
        return out

def build_default_index(encoder: Optional[SentenceTransformer] = None) -> MiniRAGIndex:
    rag = MiniRAGIndex(encoder=encoder)
    howto = [
        {"id":"HOWTO-PLAN-1", "text":"Tạo kế hoạch → '+ Tạo kế hoạch' → nhập tên → chọn Goal → Lưu.", "meta":{"type":"howto"}},
        {"id":"HOWTO-PLAN-2", "text":"Thêm bài tập → mở kế hoạch → '+ Thêm bài tập' → chọn bài → chọn ngày → Lưu.", "meta":{"type":"howto"}},
        {"id":"HOWTO-PLAN-3", "text":"Chủ nhật là 'CN' (dayOfWeek = 8). Nếu lỗi, kiểm tra enum/constraint.", "meta":{"type":"howto"}},
    ]
    ex = [
        {"id":"E#1", "text":"Plank — core stability — giảm mỡ, thể lực chung — 10-15 phút.", "meta":{"type":"exercise","exerciseId":1,"muscleGroup":"core"}},
        {"id":"E#2", "text":"Squat — lower body — tăng cơ, sức mạnh — 15-20 phút.", "meta":{"type":"exercise","exerciseId":2,"muscleGroup":"legs"}},
    ]
    rag.add_docs(howto + ex)
    return rag

# dùng chung encoder đã nạp (tránh tải model 2 lần)
INDEX = build_default_index(encoder=get_encoder())
