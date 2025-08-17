package com.ntn.repositories.impl;

import com.ntn.pojo.UserWorkoutHistory;
import com.ntn.repositories.UserWorkoutHistoryRepository;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class UserWorkoutHistoryRepositoryImpl implements UserWorkoutHistoryRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public UserWorkoutHistory save(UserWorkoutHistory h) {
        Session s = factory.getObject().getCurrentSession();
        if (h.getHistoryId() == null) {
            s.persist(h);
            return h;
        }
        return (UserWorkoutHistory) s.merge(h);
    }

    @Override
    public UserWorkoutHistory findById(Integer id) {
        return factory.getObject().getCurrentSession().get(UserWorkoutHistory.class, id);
    }

    @Override
    public void delete(UserWorkoutHistory h) {
        Session s = factory.getObject().getCurrentSession();
        s.remove(s.contains(h) ? h : s.merge(h));
    }

    private List<Predicate> preds(CriteriaBuilder cb, Root<UserWorkoutHistory> root,
                                  Integer userId, Integer planId, Integer exerciseId, String status) {
        List<Predicate> list = new ArrayList<>();
        list.add(cb.equal(root.get("userId").get("userId"), userId));
        if (planId != null)     list.add(cb.equal(root.get("planId").get("planId"), planId));
        if (exerciseId != null) list.add(cb.equal(root.get("exercisesId").get("exercisesId"), exerciseId));
        if (status != null && !status.isBlank()) list.add(cb.equal(root.get("status"), status));
        return list;
    }

    @Override
    public List<UserWorkoutHistory> findByUserIdFiltered(Integer userId, Integer planId, Integer exerciseId, String status) {
        Session s = factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<UserWorkoutHistory> cq = cb.createQuery(UserWorkoutHistory.class);
        Root<UserWorkoutHistory> root = cq.from(UserWorkoutHistory.class);

        cq.where(preds(cb, root, userId, planId, exerciseId, status).toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("completedAt")), cb.desc(root.get("historyId")));

        TypedQuery<UserWorkoutHistory> q = s.createQuery(cq);
        return q.getResultList();
    }

    @Override
    public List<UserWorkoutHistory> findByUserIdPaged(Integer userId, Integer planId, Integer exerciseId, String status, int page, int pageSize) {
        Session s = factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<UserWorkoutHistory> cq = cb.createQuery(UserWorkoutHistory.class);
        Root<UserWorkoutHistory> root = cq.from(UserWorkoutHistory.class);

        cq.where(preds(cb, root, userId, planId, exerciseId, status).toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("completedAt")), cb.desc(root.get("historyId")));

        TypedQuery<UserWorkoutHistory> q = s.createQuery(cq);
        int start = (page - 1) * pageSize;
        q.setFirstResult(start);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    @Override
    public long countByUserId(Integer userId, Integer planId, Integer exerciseId, String status) {
        Session s = factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<UserWorkoutHistory> root = cq.from(UserWorkoutHistory.class);

        cq.where(preds(cb, root, userId, planId, exerciseId, status).toArray(Predicate[]::new));
        cq.select(cb.count(root));
        return s.createQuery(cq).getSingleResult();
    }
}
