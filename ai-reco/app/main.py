import os, time
from typing import List, Optional

import httpx
from fastapi import FastAPI, HTTPException, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field, ConfigDict, model_validator

from .embed_index import reindex as _reindex, search as _search, rank as _rank, as_langchain_retriever
from langchain_openai import ChatOpenAI
from langchain.prompts import ChatPromptTemplate

# ========= ENV =========
VLLM_BASE = os.getenv("VLLM_OPENAI_BASE", "http://localhost:8001/v1")
VLLM_API_KEY = os.getenv("VLLM_OPENAI_API_KEY", "not-needed")
MODEL_NAME = os.getenv("VLLM_MODEL_NAME", "Qwen/Qwen2.5-7B-Instruct")
USE_LLM = os.getenv("USE_LLM", "true").lower() in ("1", "true", "yes")

# ========= LLM SAFE INIT =========
llm: Optional[ChatOpenAI] = None
if USE_LLM:
    http_client = None
    proxy = os.getenv("HTTPS_PROXY") or os.getenv("HTTP_PROXY")
    if proxy:
        http_client = httpx.Client(proxies=proxy, timeout=30.0)
    try:
        llm = ChatOpenAI(
            model=MODEL_NAME,
            base_url=VLLM_BASE,
            api_key=VLLM_API_KEY,  # vLLM thường bỏ qua, nhưng langchain-openai cần field này
            temperature=0.2,
            max_tokens=384,
            http_client=http_client,
        )
    except Exception as e:
        llm = None
        print(f"[AI-RECO] LLM disabled at startup: {e}")

# ========= PROMPT =========
SYS_CONCISE = (
    "Bạn là trợ lý thể hình tiếng Việt. Trả lời NGẮN GỌN, gạch đầu dòng hành động, "
    "đưa số hiệp/reps/phút khi có căn cứ; nhắc lưu ý an toàn khi cần."
)
PROMPT = ChatPromptTemplate.from_messages([
    ("system", "{sys}"),
    ("human", "Câu hỏi: {question}\n\nNgữ cảnh (top-k):\n{context}\n\nYêu cầu: trả lời tiếng Việt.")
])

# ========= APP =========
app = FastAPI(title="AI Reco", version="1.0")

# --- Handler 422 để debug dễ ---
@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    return JSONResponse(
        status_code=422,
        content={"detail": exc.errors(), "body": (await request.body()).decode("utf-8","ignore")},
    )

# ========= SCHEMAS (Pydantic v2) =========
class Item(BaseModel):
    model_config = ConfigDict(extra="allow")  # cho phép field thừa (minutes, difficulty,...)

    id: int | str
    title: str
    text: str = ""
    group: Optional[str] = None

    @model_validator(mode="before")
    @classmethod
    def _coerce_fields(cls, v):
        if not isinstance(v, dict):
            return v
        v = dict(v)
        v.setdefault("id",    v.get("exerciseId") or v.get("exercise_id") or v.get("sid") or v.get("code") or v.get("id"))
        v.setdefault("title", v.get("title") or v.get("name") or v.get("exerciseName"))
        v.setdefault("text",  v.get("text")  or v.get("description") or v.get("desc") or "")
        v.setdefault("group", v.get("group") or v.get("muscleGroup") or v.get("category"))
        return v

class ReindexIn(BaseModel):
    items: List[Item]

class RankIn(BaseModel):
    query: str
    candidates: List[Item] = Field(default_factory=list)
    topK: int = Field(10, gt=0, le=200)

class ChatIn(BaseModel):
    sessionId: str
    question: str
    topK: int = Field(4, gt=0, le=50)

# ========= UTILS =========
def _format_ctx_pairs(pairs: List[tuple]) -> str:
    lines = []
    for i, (_, it) in enumerate(pairs, 1):
        lines.append(f"[{i}] {it.get('title','')} — {it.get('text','')}")
    return "\n".join(lines)

# ========= ROUTES =========
@app.get("/health")
def health():
    return {"ok": True, "model": MODEL_NAME, "llm_ready": llm is not None, "ts": int(time.time())}

@app.post("/reindex")
def reindex(body: ReindexIn):
    n = _reindex([it.model_dump() for it in body.items])
    return {"ok": True, "indexed": n}

@app.post("/rank")
def rank(body: RankIn):
    if not body.query or not body.candidates:
        raise HTTPException(status_code=400, detail="Missing query or candidates.")
    ranked = _rank(body.query, [c.model_dump() for c in body.candidates], k=body.topK)
    return {"items": ranked}

@app.post("/chat")
def chat(body: ChatIn):
    retriever = as_langchain_retriever(k=body.topK or 4)
    if retriever:
        docs = retriever.invoke(body.question)
        context = "\n".join([f"[{i+1}] {d.page_content}" for i, d in enumerate(docs)])
        sources = [{"id": d.metadata.get("id"), "title": d.metadata.get("title"), "group": d.metadata.get("group")} for d in docs]
    else:
        pairs = _search(body.question, k=body.topK or 4)
        context = _format_ctx_pairs(pairs)
        sources = [{"id": it.get("id"), "title": it.get("title"), "group": it.get("group")} for _, it in pairs]

    messages = PROMPT.format_messages(sys=SYS_CONCISE, question=body.question, context=context)

    if llm is None:
        bullets = [
            "Tóm tắt theo ngữ cảnh trên; ưu tiên bài tập phù hợp mục tiêu/sức khỏe.",
            "Chọn 3–5 bài; 2–4 hiệp mỗi bài; 8–12 reps hoặc 30–60 giây/bài.",
            "Giãn cơ/khởi động 5–10 phút; theo dõi nhịp tim và đau bất thường.",
        ]
        return {"answer": "• " + "\n• ".join(bullets) + "\n\n(Nguồn: ngữ cảnh top-k ở trên)", "sources": sources, "model": f"{MODEL_NAME} (LLM disabled)"}

    try:
        answer = llm.invoke(messages).content.strip()
    except Exception as e:
        safe = "Tạm thời không gọi được mô hình. Bạn có thể dựa vào ngữ cảnh trên để chọn 3–5 bài phù hợp, mỗi bài 2–4 hiệp x 8–12 reps, và khởi động 5–10 phút."
        return {"answer": safe, "sources": sources, "model": f"{MODEL_NAME} (error: {type(e).__name__})"}

    return {"answer": answer, "sources": sources, "model": MODEL_NAME}
