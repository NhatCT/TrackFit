from typing import Optional

def normalize_intensity(x: Optional[str]) -> str:
    if not x:
        return "Medium"
    s = x.strip().lower()
    if s.startswith("l"): return "Low"
    if s.startswith("h"): return "High"
    return "Medium"

def map_difficulty(x: Optional[str]) -> str:
    if not x:
        return "Medium"
    s = x.strip().lower()
    if s in {"l","low","beginner","easy"}: return "Low"
    if s in {"h","high","hard","advanced"}: return "High"
    return "Medium"

def build_reason(goal: Optional[str], intensity: Optional[str], minutes: Optional[int], novelty: Optional[int]):
    parts = []
    if goal: parts.append(f"phù hợp mục tiêu “{goal}”")
    if intensity: parts.append(f"cường độ {intensity}")
    if minutes: parts.append(f"~{minutes}′")
    if novelty is not None:
        parts.append("ít trùng so với gần đây" if novelty > 0 else "quen thuộc")
    return ", ".join(parts) if parts else "phù hợp hồ sơ"
