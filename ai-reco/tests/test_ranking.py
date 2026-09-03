import os

os.environ["USE_LLM"] = "false"

import numpy as np
import pytest

pytest.importorskip("sentence_transformers")
pytest.importorskip("faiss")
pytest.importorskip("langchain_openai")

from app import main, ranker


def fake_encode(texts):
    if len(texts) == 1:
        return np.array([[1.0, 0.0]], dtype=np.float32)
    return np.array([[1.0, 0.0], [0.0, 1.0]], dtype=np.float32)


def test_personalized_rank_returns_original_items_and_reasons(monkeypatch):
    monkeypatch.setattr(ranker, "encode_texts", fake_encode)

    response = main.rank(main.RankIn(
        query="tang co",
        context={"goalType": "muscle_gain", "intensity": "High", "availableMinutes": 30},
        candidates=[
            {"id": 1, "title": "Squat", "text": "legs", "group": "legs", "minutes": 30, "difficulty": "High"},
            {"id": 2, "title": "Walk", "text": "easy", "group": "cardio", "minutes": 10, "difficulty": "Low"},
        ],
        topK=2,
    ))

    assert [item["id"] for item in response["items"]] == [1, 2]
    assert response["items"][0]["score"] > response["items"][1]["score"]
    assert "tang co" in response["items"][0]["reason"]


def test_legacy_rank_contract_remains_supported(monkeypatch):
    monkeypatch.setattr(main, "_rank", lambda query, candidates, k: [{**candidates[0], "score": 0.9}])

    response = main.rank(main.RankIn(
        query="core",
        candidates=[{"id": 3, "title": "Plank", "text": "core"}],
    ))

    assert response["items"] == [{"id": 3, "title": "Plank", "text": "core", "group": None, "score": 0.9}]
