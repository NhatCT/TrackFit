FROM python:3.11-slim

ENV PIP_NO_CACHE_DIR=1 PIP_DEFAULT_TIMEOUT=120 PYTHONUNBUFFERED=1 \
    HF_HOME=/opt/hf-cache \
    HUGGINGFACE_HUB_CACHE=/opt/hf-cache/hub \
    SENTENCE_TRANSFORMERS_HOME=/opt/hf-cache/sentencetransformers \
    TORCH_HOME=/opt/hf-cache/torch \
    XDG_CACHE_HOME=/opt/hf-cache/.xdg \
    TOKENIZERS_PARALLELISM=false

RUN apt-get update && apt-get install -y --no-install-recommends \
      libgomp1 ca-certificates curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY requirements.txt ./
RUN python -m pip install --upgrade pip setuptools wheel && \
    pip install --no-cache-dir -r requirements.txt

COPY app ./app
EXPOSE 8000
CMD ["uvicorn","app.main:app","--host","0.0.0.0","--port","8000"]
