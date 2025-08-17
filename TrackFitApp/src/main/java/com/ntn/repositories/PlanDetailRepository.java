package com.ntn.repositories;

import com.ntn.pojo.PlanDetail;
import java.util.List;

public interface PlanDetailRepository {
    PlanDetail save(PlanDetail d);
    PlanDetail findById(Integer id);
    List<PlanDetail> findByPlanId(Integer planId);
    void delete(PlanDetail d);
    Integer avgDurationByPlanAndExercise(Integer planId, Integer exerciseId);

}
