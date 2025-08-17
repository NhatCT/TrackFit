package com.ntn.repositories.impl;

import com.ntn.pojo.WorkoutPlan;
import com.ntn.repositories.WorkoutPlanRepository;
import jakarta.persistence.TypedQuery; // dùng TypedQuery cho type-safety
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class WorkoutPlanRepositoryImpl implements WorkoutPlanRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    private static final int PAGE_SIZE = 10;

    @Override
    public long countAll() {
        Session s = factory.getObject().getCurrentSession();
        var cb = s.getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        var root = cq.from(WorkoutPlan.class);
        cq.select(cb.count(root));
        return s.createQuery(cq).getSingleResult();
    }

    @Override
    public long countTemplatePlans() {
        Session s = factory.getObject().getCurrentSession();
        var cb = s.getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        var root = cq.from(WorkoutPlan.class);
        cq.select(cb.count(root));
        cq.where(cb.isTrue(root.get("isTemplate")));
        return s.createQuery(cq).getSingleResult();
    }

    @Override
    public WorkoutPlan save(WorkoutPlan p) {
        Session s = factory.getObject().getCurrentSession();
        if (p.getPlanId() == null) {
            s.persist(p);
            return p;
        } else {
            return (WorkoutPlan) s.merge(p);
        }
    }

    @Override
    public WorkoutPlan findById(Integer id) {
        return factory.getObject().getCurrentSession().get(WorkoutPlan.class, id);
    }

    @Override
    public List<WorkoutPlan> findByUserId(Integer userId) {
        Session s = factory.getObject().getCurrentSession();
        // Dùng NamedQuery có sẵn + TypedQuery
        TypedQuery<WorkoutPlan> q = s.createNamedQuery("WorkoutPlan.findByUserId", WorkoutPlan.class);
        q.setParameter("userId", userId);
        return q.getResultList();
    }

    @Override
    public void delete(WorkoutPlan p) {
        Session s = factory.getObject().getCurrentSession();
        s.remove(s.contains(p) ? p : s.merge(p));
    }

    // ===== PHÂN TRANG THEO USER (kw trên planName, page 1-based) =====
    @Override
    public List<WorkoutPlan> getPlansByUser(Integer userId, Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<WorkoutPlan> cq = cb.createQuery(WorkoutPlan.class);
        Root<WorkoutPlan> root = cq.from(WorkoutPlan.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("userId").get("userId"), userId));

        String kw = params != null ? params.get("kw") : null;
        if (kw != null && !kw.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("planName")), "%" + kw.trim().toLowerCase() + "%"));
        }

        cq.where(predicates.toArray(Predicate[]::new));

        // sort mặc định: mới nhất trước (fallback theo id)
        try {
            cq.orderBy(cb.desc(root.get("createdAt")));
        } catch (Exception ignore) {
            cq.orderBy(cb.desc(root.get("planId")));
        }

        TypedQuery<WorkoutPlan> q = s.createQuery(cq);

        int page = 1;
        if (params != null && params.get("page") != null) {
            try {
                page = Math.max(Integer.parseInt(params.get("page")), 1);
            } catch (NumberFormatException ignore) {
            }
        }
        int start = (page - 1) * PAGE_SIZE;
        q.setFirstResult(start);
        q.setMaxResults(PAGE_SIZE);

        return q.getResultList();
    }

    @Override
    public long countPlansByUser(Integer userId, Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<WorkoutPlan> root = cq.from(WorkoutPlan.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("userId").get("userId"), userId));

        String kw = params != null ? params.get("kw") : null;
        if (kw != null && !kw.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("planName")), "%" + kw.trim().toLowerCase() + "%"));
        }

        cq.where(predicates.toArray(Predicate[]::new));
        cq.select(cb.count(root));

        TypedQuery<Long> q = s.createQuery(cq);
        return q.getSingleResult();
    }
}
