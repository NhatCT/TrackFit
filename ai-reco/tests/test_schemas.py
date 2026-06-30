import pytest
from app.schemas import (
    UserInfo, Candidate, AiRankRequest, AiRankedExercise, EmbedRequest
)


class TestUserInfo:
    def test_defaults(self):
        user = UserInfo()
        assert user.id is None
        assert user.goalType is None
        assert user.gender is None

    def test_with_values(self):
        user = UserInfo(id=1, goalType="muscle_gain", gender="Male")
        assert user.id == 1
        assert user.goalType == "muscle_gain"
        assert user.gender == "Male"


class TestCandidate:
    def test_required_exercise_id(self):
        c = Candidate(exerciseId=42)
        assert c.exerciseId == 42
        assert c.name is None
        assert c.muscleGroup is None
        assert c.minutes is None
        assert c.difficulty is None

    def test_full_candidate(self):
        c = Candidate(
            exerciseId=1,
            name="Bench Press",
            muscleGroup="Chest",
            minutes=15,
            difficulty="High"
        )
        assert c.name == "Bench Press"
        assert c.muscleGroup == "Chest"
        assert c.minutes == 15
        assert c.difficulty == "High"

    def test_missing_exercise_id_raises(self):
        with pytest.raises(Exception):
            Candidate()


class TestAiRankRequest:
    def test_empty_request(self):
        req = AiRankRequest()
        assert req.user is None
        assert req.context is None
        assert req.candidates == []

    def test_with_candidates(self):
        req = AiRankRequest(
            user=UserInfo(id=1, goalType="cardio"),
            context={"intensity": "High", "availableMinutes": 30},
            candidates=[
                Candidate(exerciseId=1, name="Run"),
                Candidate(exerciseId=2, name="Squat"),
            ]
        )
        assert len(req.candidates) == 2
        assert req.context["intensity"] == "High"


class TestAiRankedExercise:
    def test_creation(self):
        ex = AiRankedExercise(exerciseId=1, score=0.85, reason="good match")
        assert ex.exerciseId == 1
        assert ex.score == 0.85
        assert ex.reason == "good match"

    def test_reason_optional(self):
        ex = AiRankedExercise(exerciseId=2, score=0.5)
        assert ex.reason is None


class TestEmbedRequest:
    def test_creation(self):
        req = EmbedRequest(texts=["hello", "world"])
        assert len(req.texts) == 2

    def test_empty_list(self):
        req = EmbedRequest(texts=[])
        assert req.texts == []
