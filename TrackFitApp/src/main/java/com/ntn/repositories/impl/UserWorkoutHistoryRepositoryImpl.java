package com.ntn.repositories.impl;

import com.ntn.pojo.UserWorkoutHistory;
import com.ntn.repositories.UserWorkoutHistoryRepository;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@Transactional
public class UserWorkoutHistoryRepositoryImpl implements UserWorkoutHistoryRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public List<UserWorkoutHistory> findBetweenAll(Date from, Date to, String status) {
        Session s = factory.getObject().getCurrentSession();
        var cb = s.getCriteriaBuilder();
        var cq = cb.createQuery(UserWorkoutHistory.class);
        var root = cq.from(UserWorkoutHistory.class);

        List<Predicate> preds = new ArrayList<>();
        if (from != null && to != null) {
            preds.add(cb.between(root.get("completedAt"), from, to));
        } else if (from != null) {
            preds.add(cb.greaterThanOrEqualTo(root.get("completedAt"), from));
        } else if (to != null) {
            preds.add(cb.lessThanOrEqualTo(root.get("completedAt"), to));
        }
        if (status != null && !status.isBlank()) {
            preds.add(cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
        }
        if (!preds.isEmpty()) {
            cq.where(preds.toArray(Predicate[]::new));
        }
        cq.orderBy(cb.asc(root.get("completedAt")));

        return s.createQuery(cq).getResultList();
    }
@Override
public UserWorkoutHistory save(UserWorkoutHistory h) {
        Session s = factory.getObject().getCurrentSession();
        if (h.getHistoryId() == null) { s.persist(h); return h; }
        return (UserWorkoutHistory) s.merge(h);
    }

    @Override
public UserWorkoutHistory

findById(Integer id) {
        return factory.getObject().getCurrentSession().get(UserWorkoutHistory.class  

, id);
    }

    @Override
public void delete(UserWorkoutHistory h) {
        Session s = factory.getObject().getCurrentSession();
        s.remove(s.contains(h) ? h : s.merge(h));
    }

    @Override
public List<UserWorkoutHistory> findByUserIdFiltered(Integer userId, Integer planId, Integer exerciseId, String status) {
        return queryBase(userId, planId, exerciseId, status, null, null);
    }

    @Override
public List<UserWorkoutHistory> findByUserIdPaged(Integer userId, Integer planId, Integer exerciseId, String status, int page, int pageSize) {
        int start = Math.max(page, 1) - 1;
        return queryBase(userId, planId, exerciseId, status, start * pageSize, pageSize);
    }

    @Override
public long countByUserId(Integer userId, Integer planId, Integer exerciseId, String status) {
        Session s = factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery

<Long> cq = cb.createQuery(Long.class  

);
        Root

<UserWorkoutHistory> root = cq.from(UserWorkoutHistory.class  

);

        List<Predicate> ps = buildPreds(cb, root, userId, planId, exerciseId, status, null, null);

        cq.select(cb.count(root)).where(ps.toArray(Predicate[]::new));
        return s.createQuery(cq).getSingleResult();
    }

    @Override
public long countCompletedBetween(Integer userId, Date from, Date to) {
        Session s = factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery

<Long> cq = cb.createQuery(Long.class  

);
        Root

<UserWorkoutHistory> root = cq.from(UserWorkoutHistory.class  

);

        List<Predicate> ps = buildPreds(cb, root, userId, null, null, "COMPLETED", from, to);
        cq.select(cb.count(root)).where(ps.toArray(Predicate[]::new));
        return s.createQuery(cq).getSingleResult();
    }

    @Override
public List<UserWorkoutHistory> findBetween(Integer userId, Date from, Date to, String status) {
        Session s = factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery

<UserWorkoutHistory> cq = cb.createQuery(UserWorkoutHistory.class  

);
        Root

<UserWorkoutHistory> root = cq.from(UserWorkoutHistory.class  

);

        List<Predicate> ps = buildPreds(cb, root, userId, null, null, status, from, to);
        cq.select(root).where(ps.toArray(Predicate[]::new));
        cq.orderBy(cb.asc(root.get("completedAt")), cb.asc(root.get("historyId")));

        return s.createQuery(cq).getResultList();
    }

    // ==== helpers ====
    private List<UserWorkoutHistory> queryBase(Integer userId, Integer planId, Integer exerciseId, String status, Integer first, Integer max) {
        Session s = factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery

<UserWorkoutHistory> cq = cb.createQuery(UserWorkoutHistory.class  

);
        Root

<UserWorkoutHistory> root = cq.from(UserWorkoutHistory.class  

);

        List<Predicate> ps = buildPreds(cb, root, userId, planId, exerciseId, status, null, null);
        cq.select(root).where(ps.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("completedAt")), cb.desc(root.get("historyId")));

        Query<UserWorkoutHistory> q = s.createQuery(cq);
        if (first != null && max != null) {
            q.setFirstResult(first);
            q.setMaxResults(max);
        }
        return q.getResultList();
    }

    private List<Predicate> buildPreds(CriteriaBuilder cb, Root<UserWorkoutHistory> root,
                                       Integer userId, Integer planId, Integer exerciseId, String status,
                                       Date from, Date to) {
        List<Predicate> ps = new ArrayList<>();
        if (userId != null) ps.add(cb.equal(root.get("userId").get("userId"), userId));
        if (planId != null) ps.add(cb.equal(root.get("planId").get("planId"), planId));
        if (exerciseId != null) ps.add(cb.equal(root.get("exercisesId").get("exercisesId"), exerciseId));
        if (status != null && !status.isBlank()) ps.add(cb.equal(root.get("status"), status));
        if (from != null) ps.add(cb.greaterThanOrEqualTo(root.get("completedAt"), from));
        if (to != null) ps.add(cb.lessThanOrEqualTo(root.get("completedAt"), to));
        return ps;
    }
}
