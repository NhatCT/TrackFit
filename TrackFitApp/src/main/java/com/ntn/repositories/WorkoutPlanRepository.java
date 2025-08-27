package com.ntn.repositories;

import com.ntn.pojo.WorkoutPlan;
import java.util.List;
import java.util.Map;

public interface WorkoutPlanRepository {
    WorkoutPlan save(WorkoutPlan p);
    WorkoutPlan findById(Integer id);
    List<WorkoutPlan> findByUserId(Integer userId);
    void delete(WorkoutPlan p);

    // USER listing có phân trang/tìm kiếm
    List<WorkoutPlan> getPlansByUser(Integer userId, Map<String,String> params);
    long countPlansByUser(Integer userId, Map<String,String> params);

    // Thống kê
    long countAll();
    long countTemplatePlans();

    // ADMIN listing toàn hệ thống
    List<WorkoutPlan> getPlans(Map<String,String> params);
    long countPlans(Map<String,String> params);
}
