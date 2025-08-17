package com.ntn.repositories;

import com.ntn.pojo.WorkoutPlan;
import java.util.List;

public interface WorkoutPlanRepository {
    WorkoutPlan save(WorkoutPlan p);
    WorkoutPlan findById(Integer id);
    List<WorkoutPlan> findByUserId(Integer userId);
    void delete(WorkoutPlan p);
    List<WorkoutPlan> getPlansByUser(Integer userId, java.util.Map<String,String> params);
    long countPlansByUser(Integer userId, java.util.Map<String,String> params);
    long countAll();
    long countTemplatePlans();
}
