package com.ntn.services;

import com.ntn.dto.RecommendationParamsDTO;

public interface AiAdviceService {

    int sendAdviceFromReco(String username, RecommendationParamsDTO params, int top);

    int sendAdviceFromRecoIfNotExists(String username, com.ntn.dto.RecommendationParamsDTO params,
            int top, int withinDays);
}
