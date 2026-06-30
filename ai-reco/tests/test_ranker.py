import sys
from unittest.mock import MagicMock, patch

import pytest

# Mock the heavy ML dependencies before importing ranker
mock_st = MagicMock()
sys.modules['sentence_transformers'] = mock_st
sys.modules['faiss'] = MagicMock()

from app.ranker import candidate_to_text, context_to_query
from app.schemas import AiRankRequest, UserInfo, Candidate


class TestCandidateToText:
    def test_full_candidate(self):
        c = Candidate(exerciseId=1, name="Bench Press", muscleGroup="Chest", minutes=15, difficulty="Hard")
        text = candidate_to_text(c)
        assert "Bench Press" in text
        assert "Chest" in text
        assert "15" in text
        assert "Hard" in text

    def test_minimal_candidate(self):
        c = Candidate(exerciseId=1, name=None, muscleGroup=None, minutes=None, difficulty=None)
        text = candidate_to_text(c)
        assert isinstance(text, str)

    def test_partial_candidate(self):
        c = Candidate(exerciseId=1, name="Squat", muscleGroup="Legs")
        text = candidate_to_text(c)
        assert "Squat" in text
        assert "Legs" in text


class TestContextToQuery:
    def test_full_context(self):
        req = AiRankRequest(
            user=UserInfo(id=1, goalType="weight_loss"),
            context={"intensity": "High", "availableMinutes": 45, "kw": "chest"},
            candidates=[]
        )
        query = context_to_query(req)
        assert "weight_loss" in query
        assert "High" in query
        assert "45" in query
        assert "chest" in query

    def test_no_user_goal_uses_context_goal(self):
        req = AiRankRequest(
            user=None,
            context={"goalType": "cardio", "intensity": "low"},
            candidates=[]
        )
        query = context_to_query(req)
        assert "cardio" in query
        assert "Low" in query

    def test_empty_context(self):
        req = AiRankRequest(user=None, context=None, candidates=[])
        query = context_to_query(req)
        assert "general" in query
        assert "Medium" in query
        assert "25" in query

    def test_no_keyword(self):
        req = AiRankRequest(
            user=UserInfo(goalType="muscle_gain"),
            context={"intensity": "Medium"},
            candidates=[]
        )
        query = context_to_query(req)
        assert "muscle_gain" in query
        assert "query:" not in query
