import pytest
from app.utils import normalize_intensity, map_difficulty, build_reason


class TestNormalizeIntensity:
    def test_none_returns_medium(self):
        assert normalize_intensity(None) == "Medium"

    def test_empty_returns_medium(self):
        assert normalize_intensity("") == "Medium"

    def test_low_variations(self):
        assert normalize_intensity("low") == "Low"
        assert normalize_intensity("Low") == "Low"
        assert normalize_intensity("light") == "Low"
        assert normalize_intensity("  low  ") == "Low"

    def test_high_variations(self):
        assert normalize_intensity("high") == "High"
        assert normalize_intensity("High") == "High"
        assert normalize_intensity("heavy") == "High"
        assert normalize_intensity("  HIGH  ") == "High"

    def test_medium_default(self):
        assert normalize_intensity("medium") == "Medium"
        assert normalize_intensity("moderate") == "Medium"
        assert normalize_intensity("normal") == "Medium"


class TestMapDifficulty:
    def test_none_returns_medium(self):
        assert map_difficulty(None) == "Medium"

    def test_empty_returns_medium(self):
        assert map_difficulty("") == "Medium"

    def test_low_variants(self):
        assert map_difficulty("l") == "Low"
        assert map_difficulty("low") == "Low"
        assert map_difficulty("beginner") == "Low"
        assert map_difficulty("easy") == "Low"
        assert map_difficulty("Low") == "Low"

    def test_high_variants(self):
        assert map_difficulty("h") == "High"
        assert map_difficulty("high") == "High"
        assert map_difficulty("hard") == "High"
        assert map_difficulty("advanced") == "High"

    def test_medium_default(self):
        assert map_difficulty("medium") == "Medium"
        assert map_difficulty("intermediate") == "Medium"
        assert map_difficulty("m") == "Medium"


class TestBuildReason:
    def test_all_params(self):
        reason = build_reason("weight_loss", "High", 30, 1)
        assert "weight_loss" in reason
        assert "High" in reason
        assert "30" in reason
        assert "ít trùng" in reason

    def test_no_novelty_shows_familiar(self):
        reason = build_reason("muscle_gain", "Medium", 25, 0)
        assert "quen thuộc" in reason

    def test_all_none_returns_default(self):
        reason = build_reason(None, None, None, None)
        assert reason == "phù hợp hồ sơ"

    def test_only_goal(self):
        reason = build_reason("cardio", None, None, None)
        assert "cardio" in reason

    def test_only_minutes(self):
        reason = build_reason(None, None, 45, None)
        assert "45" in reason
