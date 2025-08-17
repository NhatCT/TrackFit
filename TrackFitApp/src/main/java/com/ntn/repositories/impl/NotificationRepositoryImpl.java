package com.ntn.repositories.impl;

import com.ntn.pojo.Notification;
import com.ntn.repositories.NotificationRepository;
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
public class NotificationRepositoryImpl implements NotificationRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public Notification save(Notification n) {
        Session s = factory.getObject().getCurrentSession();
        if (n.getNotificationId() == null) {
            s.persist(n);
            return n;
        }
        return (Notification) s.merge(n);
    }

    @Override
    public Notification findById(Integer id) {
        return factory.getObject().getCurrentSession().get(Notification.class, id);
    }

    @Override
    public void delete(Notification n) {
        Session s = factory.getObject().getCurrentSession();
        s.remove(s.contains(n) ? n : s.merge(n));
    }

    @Override
    public List<Notification> findByUserIdFiltered(Integer userId, Boolean isRead, String type, String kw) {
        Session s = factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<Notification> cq = cb.createQuery(Notification.class);
        Root<Notification> root = cq.from(Notification.class);

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("userId").get("userId"), userId));
        if (isRead != null) preds.add(cb.equal(root.get("isRead"), isRead));
        if (type != null && !type.isBlank()) preds.add(cb.equal(root.get("type"), type));
        if (kw != null && !kw.isBlank())
            preds.add(cb.like(cb.lower(root.get("message")), "%" + kw.trim().toLowerCase() + "%"));

        cq.where(preds.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")));

        TypedQuery<Notification> q = s.createQuery(cq);
        return q.getResultList();
    }

    @Override
    public List<Notification> findByUserIdPaged(Integer userId, Boolean isRead, String type, String kw, int page, int pageSize) {
        Session s = factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<Notification> cq = cb.createQuery(Notification.class);
        Root<Notification> root = cq.from(Notification.class);

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("userId").get("userId"), userId));
        if (isRead != null) preds.add(cb.equal(root.get("isRead"), isRead));
        if (type != null && !type.isBlank()) preds.add(cb.equal(root.get("type"), type));
        if (kw != null && !kw.isBlank())
            preds.add(cb.like(cb.lower(root.get("message")), "%" + kw.trim().toLowerCase() + "%"));

        cq.where(preds.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")));

        TypedQuery<Notification> q = s.createQuery(cq);
        int start = (page - 1) * pageSize;
        q.setFirstResult(start);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    @Override
    public long countByUserId(Integer userId, Boolean isRead, String type, String kw) {
        Session s = factory.getObject().getCurrentSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Notification> root = cq.from(Notification.class);

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("userId").get("userId"), userId));
        if (isRead != null) preds.add(cb.equal(root.get("isRead"), isRead));
        if (type != null && !type.isBlank()) preds.add(cb.equal(root.get("type"), type));
        if (kw != null && !kw.isBlank())
            preds.add(cb.like(cb.lower(root.get("message")), "%" + kw.trim().toLowerCase() + "%"));

        cq.where(preds.toArray(Predicate[]::new));
        cq.select(cb.count(root));
        return s.createQuery(cq).getSingleResult();
    }
}
