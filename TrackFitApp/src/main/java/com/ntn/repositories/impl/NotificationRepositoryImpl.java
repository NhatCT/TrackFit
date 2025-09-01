package com.ntn.repositories.impl;

import com.ntn.pojo.Notification;
import com.ntn.repositories.NotificationRepository;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
@Transactional
public class NotificationRepositoryImpl implements NotificationRepository {

    @Autowired
    private LocalSessionFactoryBean factory;

    @Override
    public Notification save(Notification n) {
        Session s = factory.getObject().getCurrentSession();
        if (n.getNotificationId() == null) { s.persist(n); return n; }
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
        if (type != null && !type.isBlank()) preds.add(cb.equal(cb.upper(root.get("type")), type.toUpperCase()));
        if (kw != null && !kw.isBlank()) {
            String like = "%" + kw.trim().toLowerCase() + "%";
            preds.add(cb.or(
                    cb.like(cb.lower(root.get("message")), like),
                    cb.like(cb.lower(root.get("sender")), like)
            ));
        }

        cq.where(preds.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")), cb.desc(root.get("notificationId")));
        return s.createQuery(cq).getResultList();
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
        if (type != null && !type.isBlank()) preds.add(cb.equal(cb.upper(root.get("type")), type.toUpperCase()));
        if (kw != null && !kw.isBlank()) {
            String like = "%" + kw.trim().toLowerCase() + "%";
            preds.add(cb.or(
                    cb.like(cb.lower(root.get("message")), like),
                    cb.like(cb.lower(root.get("sender")), like)
            ));
        }

        cq.where(preds.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")), cb.desc(root.get("notificationId")));

        TypedQuery<Notification> q = s.createQuery(cq);
        int start = Math.max(page, 1) - 1;
        q.setFirstResult(start * pageSize);
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
        if (type != null && !type.isBlank()) preds.add(cb.equal(cb.upper(root.get("type")), type.toUpperCase()));
        if (kw != null && !kw.isBlank()) {
            String like = "%" + kw.trim().toLowerCase() + "%";
            preds.add(cb.or(
                    cb.like(cb.lower(root.get("message")), like),
                    cb.like(cb.lower(root.get("sender")), like)
            ));
        }

        cq.where(preds.toArray(Predicate[]::new));
        cq.select(cb.count(root));
        return s.createQuery(cq).getSingleResult();
    }

    // ====== mở rộng ======
    @Override
    public long countUnread(Integer userId) {
        Session s = factory.getObject().getCurrentSession();
        String jpql = "select count(n) from Notification n where n.userId.userId=:uid and n.isRead=false";
        return s.createQuery(jpql, Long.class).setParameter("uid", userId).getSingleResult();
    }

    @Override
    public int markAllRead(Integer userId) {
        Session s = factory.getObject().getCurrentSession();
        Query<?> q = s.createQuery("update Notification n set n.isRead=true where n.userId.userId=:uid and n.isRead=false");
        q.setParameter("uid", userId);
        return q.executeUpdate();
    }

    @Override
    public int cleanupReadOlderThan(Integer userId, Date olderThan) {
        Session s = factory.getObject().getCurrentSession();
        Query<?> q = s.createQuery("delete from Notification n where n.userId.userId=:uid and n.isRead=true and n.createdAt < :d");
        q.setParameter("uid", userId);
        q.setParameter("d", olderThan);
        return q.executeUpdate();
    }
}
