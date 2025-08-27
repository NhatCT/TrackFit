package com.ntn.services;

import com.ntn.dto.*;
import java.util.Map;

public interface WorkoutPlanService {
    // USER scope
    WorkoutPlanResponseDTO createPlan(String username, WorkoutPlanCreateRequest req);
    WorkoutPlanResponseDTO getPlan(Integer planId);
    Map<String, Object> listPlansByUserPaged(String username, Integer page, Integer pageSize, String kw);
    void deletePlan(String username, Integer planId);

    // Detail (dùng chung)
    PlanDetailViewDTO addDetail(Integer planId, PlanDetailItemDTO dto);
    PlanDetailViewDTO updateDetail(Integer detailId, PlanDetailItemDTO dto);
    void deleteDetail(Integer detailId);

    // ADMIN list toàn hệ thống
    Map<String,Object> listAllPlansPaged(Integer page, Integer pageSize, String kw);

    // ADMIN tạo/xoá/cập nhật
    WorkoutPlanResponseDTO createPlanForUser(Integer userId, WorkoutPlanCreateRequest req);
    void deletePlanAdmin(Integer planId);
    WorkoutPlanResponseDTO updatePlanAdmin(Integer planId, WorkoutPlanCreateRequest req);
}
