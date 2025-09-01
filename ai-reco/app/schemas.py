from typing import Optional, List, Dict, Any
from pydantic import BaseModel, Field

class UserInfo(BaseModel):
    id: Optional[int] = None
    goalType: Optional[str] = None
    gender: Optional[str] = None

class Candidate(BaseModel):
    exerciseId: int
    name: Optional[str] = None
    muscleGroup: Optional[str] = None
    minutes: Optional[int] = None
    difficulty: Optional[str] = None

class AiRankRequest(BaseModel):
    user: Optional[UserInfo] = None
    context: Optional[Dict[str, Any]] = None  # có thể chứa: kw, availableMinutes, intensity, recentIds, ...
    candidates: List[Candidate] = Field(default_factory=list)

class AiRankedExercise(BaseModel):
    exerciseId: int
    score: float
    reason: Optional[str] = None

class EmbedRequest(BaseModel):
    texts: List[str]
