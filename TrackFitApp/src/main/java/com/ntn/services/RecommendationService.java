// com/ntn/services/RecommendationService.java
package com.ntn.services;

import com.ntn.dto.RecommendationItemDTO;
import com.ntn.dto.RecommendationParamsDTO;
import java.util.List;

public interface RecommendationService {
    List<RecommendationItemDTO> recommendExercises(String username, RecommendationParamsDTO params);
}
