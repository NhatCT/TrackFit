FROM python:3.11-slim

ENV PIP_NO_CACHE_DIR=1 PIP_DEFAULT_TIMEOUT=120 PYTHONUNBUFFERED=1 \
    HF_HOME=/opt/hf-cache HUGGINGFACE_HUB_CACHE=/opt/hf-cache/hub \
    TRANSFORMERS_CACHE=/opt/hf-cache/transformers \
    SENTENCE_TRANSFORMERS_HOME=/opt/hf-cache/sentencetransformers \
    TORCH_HOME=/opt/hf-cache/torch XDG_CACHE_HOME=/opt/hf-cache/.xdg \
    TOKENIZERS_PARALLELISM=false

# Thêm curl để healthcheck, và libgomp1 cho faiss
RUN apt-get update && apt-get install -y --no-install-recommends \
      libgomp1 ca-certificates curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY requirements.txt .

RUN python -m pip install --upgrade pip setuptools wheel && \
    pip install --no-cache-dir --index-url https://download.pytorch.org/whl/cpu \
      torch torchvision torchaudio && \
    pip install --no-cache-dir -r requirements.txt

COPY . .
RUN mkdir -p /opt/hf-cache/hub /opt/hf-cache/transformers \
             /opt/hf-cache/sentencetransformers /opt/hf-cache/torch /opt/hf-cache/.xdg

EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
