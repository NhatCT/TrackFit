package com.ntn.services;

import com.ntn.dto.*;
import java.util.List;

public interface AiRecoService {
    boolean reindexManual(java.util.List<RankCandidateDTO> items);
    List<RankResponseItemDTO> rankExercises(String query,List<RankCandidateDTO> candidates, Integer topK);
    ChatAnswerDTO chatAsk(String sessionId, String question, Integer topK);
    boolean aiHealth();
}
