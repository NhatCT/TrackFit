package com.ntn.repositories.impl;

import com.ntn.pojo.PlanDetail;
import com.ntn.repositories.PlanDetailRepository;
import jakarta.persistence.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class PlanDetailRepositoryImpl implements PlanDetailRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public PlanDetail save(PlanDetail d) {
        Session s = factory.getObject().getCurrentSession();
        if (d.getDetailId() == null) {
            s.persist(d);
            return d;
        } else {
            return (PlanDetail) s.merge(d);
        }
    }

    @Override
    public PlanDetail findById(Integer id) {
        Session s = factory.getObject().getCurrentSession();
        return s.get(PlanDetail.class, id);
    }

    @Override
    public List<PlanDetail> findByPlanId(Integer planId) {
        Session s = factory.getObject().getCurrentSession();
        Query q = s.createNamedQuery("PlanDetail.findByPlanId", PlanDetail.class);
        q.setParameter("planId", planId);
        return q.getResultList();
    }

    @Override
    public void delete(PlanDetail d) {
        Session s = factory.getObject().getCurrentSession();
        s.remove(s.contains(d) ? d : s.merge(d));
    }
}
