package com.ntn.services;

import com.ntn.dto.RecommendationParamsDTO;

public interface AiAdviceService {
    int sendAdviceFromRecoIfNotExists(String username,
                                      RecommendationParamsDTO params,
                                      int top,
                                      int withinDays);
}
