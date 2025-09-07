package com.ntn.repositories.impl;

import com.ntn.pojo.WorkoutPlan;
import com.ntn.repositories.WorkoutPlanRepository;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
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
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<WorkoutPlan> root = cq.from(WorkoutPlan.class);
        cq.select(cb.count(root));
        return s.createQuery(cq).getSingleResult();
    }

    @Override
    public long countTemplatePlans() {
        Session s = factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<WorkoutPlan> root = cq.from(WorkoutPlan.class);
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
        TypedQuery<WorkoutPlan> q = s.createNamedQuery("WorkoutPlan.findByUserId", WorkoutPlan.class);
        q.setParameter("userId", userId);
        return q.getResultList();
    }

    @Override
    public void delete(WorkoutPlan p) {
        Session s = factory.getObject().getCurrentSession();
        s.remove(s.contains(p) ? p : s.merge(p));
    }

    // ===== USER list + phân trang (kw trên planName) =====
    @Override
    public List<WorkoutPlan> getPlansByUser(Integer userId, Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<WorkoutPlan> cq = cb.createQuery(WorkoutPlan.class);
        Root<WorkoutPlan> root = cq.from(WorkoutPlan.class);
        root.fetch("userId");
        root.fetch("goalId", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("userId").get("userId"), userId));

        String kw = params != null ? params.get("kw") : null;
        if (kw != null && !kw.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("planName")), "%" + kw.trim().toLowerCase() + "%"));
        }

        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")));

        int page = 1, pageSize = PAGE_SIZE;
        if (params != null) {
            if (params.get("page") != null) try { page = Math.max(Integer.parseInt(params.get("page")), 1); } catch (Exception ignore) {}
            if (params.get("pageSize") != null) try { pageSize = Math.max(Integer.parseInt(params.get("pageSize")), 1); } catch (Exception ignore) {}
        }

        TypedQuery<WorkoutPlan> q = s.createQuery(cq.select(root).distinct(true));
        q.setFirstResult((page - 1) * pageSize);
        q.setMaxResults(pageSize);
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
        cq.select(cb.countDistinct(root));
        return s.createQuery(cq).getSingleResult();
    }

    @Override
    public List<WorkoutPlan> getPlans(Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<WorkoutPlan> cq = cb.createQuery(WorkoutPlan.class);
        Root<WorkoutPlan> root = cq.from(WorkoutPlan.class);
        Join<Object, Object> userJoin = root.join("userId");
        Join<Object, Object> goalJoin = root.join("goalId", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        String kwRaw = params != null ? params.get("kw") : null;
        if (kwRaw != null && !kwRaw.trim().isEmpty()) {
            String kw = kwRaw.trim().toLowerCase();
            String like = "%" + kw + "%";
            Integer durNum = null;
            try {
                if (kw.matches("^\\d+$")) durNum = Integer.parseInt(kw);
            } catch (Exception ignore) {}

            Predicate goalPred =
                cb.or(
                    cb.like(cb.lower(goalJoin.get("goalType")), like),
                    cb.like(cb.lower(goalJoin.get("intensity")), like)
                );
            if (durNum != null) {
                goalPred = cb.or(goalPred, cb.equal(goalJoin.get("workoutDuration"), durNum));
            }

            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("planName")), like),
                    cb.like(cb.lower(userJoin.get("username")), like),
                    cb.like(cb.lower(userJoin.get("firstName")), like),
                    cb.like(cb.lower(userJoin.get("lastName")), like),
                    goalPred
                )
            );
        }

        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")));

        int page = 1, pageSize = PAGE_SIZE;
        if (params != null) {
            if (params.get("page") != null) try { page = Math.max(Integer.parseInt(params.get("page")), 1); } catch (Exception ignore) {}
            if (params.get("pageSize") != null) try { pageSize = Math.max(Integer.parseInt(params.get("pageSize")), 1); } catch (Exception ignore) {}
        }

        TypedQuery<WorkoutPlan> q = s.createQuery(cq.select(root).distinct(true));
        q.setFirstResult((page - 1) * pageSize);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    @Override
    public long countPlans(Map<String, String> params) {
        Session s = this.factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<WorkoutPlan> root = cq.from(WorkoutPlan.class);

        Join<Object, Object> userJoin = root.join("userId");
        Join<Object, Object> goalJoin = root.join("goalId", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        String kwRaw = params != null ? params.get("kw") : null;
        if (kwRaw != null && !kwRaw.trim().isEmpty()) {
            String kw = kwRaw.trim().toLowerCase();
            String like = "%" + kw + "%";

            Integer durNum = null;
            try {
                if (kw.matches("^\\d+$")) durNum = Integer.parseInt(kw);
            } catch (Exception ignore) {}

            Predicate goalPred =
                cb.or(
                    cb.like(cb.lower(goalJoin.get("goalType")), like),
                    cb.like(cb.lower(goalJoin.get("intensity")), like)
                );
            if (durNum != null) {
                goalPred = cb.or(goalPred, cb.equal(goalJoin.get("workoutDuration"), durNum));
            }

            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("planName")), like),
                    cb.like(cb.lower(userJoin.get("username")), like),
                    cb.like(cb.lower(userJoin.get("firstName")), like),
                    cb.like(cb.lower(userJoin.get("lastName")), like),
                    goalPred
                )
            );
        }

        cq.where(predicates.toArray(Predicate[]::new));
        cq.select(cb.countDistinct(root));
        return s.createQuery(cq).getSingleResult();
    }
}
