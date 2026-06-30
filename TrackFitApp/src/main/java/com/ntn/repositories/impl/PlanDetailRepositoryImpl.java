    package com.ntn.repositories.impl;

import com.ntn.pojo.PlanDetail;
import com.ntn.repositories.PlanDetailRepository;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class PlanDetailRepositoryImpl extends BaseHibernateRepository implements PlanDetailRepository {

    @Override
    public PlanDetail save(PlanDetail d) {
        return saveOrMerge(d, d.getDetailId());
    }

    @Override
    public PlanDetail findById(Integer id) {
        return currentSession().get(PlanDetail.class, id);
    }

    @Override
    public List<PlanDetail> findByPlanId(Integer planId) {
        var s = currentSession();
        Query q = s.createNamedQuery("PlanDetail.findByPlanId", PlanDetail.class);
        q.setParameter("planId", planId);
        return q.getResultList();
    }

    @Override
    public void delete(PlanDetail d) {
        removeEntity(d);
    }
  @Override
public Integer avgDurationByPlanAndExercise(Integer planId, Integer exerciseId) {
    var s = currentSession();
    var cb = s.getCriteriaBuilder();

    var cq = cb.createQuery(Double.class);
    var root = cq.from(PlanDetail.class);

    cq.select(cb.avg(root.get("duration"))); 
    cq.where(
        cb.equal(root.get("planId").get("planId"), planId),
        cb.equal(root.get("exercisesId").get("exercisesId"), exerciseId)
    );

    Double result = s.createQuery(cq).getSingleResult();
    return (result == null) ? 0 : (int) Math.round(result);
}

}
