package com.ntn.services;

import com.ntn.dto.*;
import java.util.Map;

public interface WorkoutPlanService {

    WorkoutPlanResponseDTO createPlan(String username, WorkoutPlanCreateRequest req);

    WorkoutPlanResponseDTO getPlan(Integer planId);

    Map<String, Object> listPlansByUserPaged(String username, Integer page, Integer pageSize, String kw);

    void deletePlan(String username, Integer planId);

    PlanDetailViewDTO addDetail(Integer planId, PlanDetailItemDTO dto);

    PlanDetailViewDTO updateDetail(Integer detailId, PlanDetailItemDTO dto);

    void deleteDetail(Integer detailId);

    Map<String, Object> listAllPlansPaged(Integer page, Integer pageSize, String kw);

    WorkoutPlanResponseDTO createPlanForUser(Integer userId, WorkoutPlanCreateRequest req);

    void deletePlanAdmin(Integer planId);

    WorkoutPlanResponseDTO updatePlanAdmin(Integer planId, WorkoutPlanCreateRequest req);
}
