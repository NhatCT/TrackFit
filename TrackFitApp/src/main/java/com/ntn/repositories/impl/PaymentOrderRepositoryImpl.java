package com.ntn.repositories.impl;

import com.ntn.pojo.PaymentOrder;
import com.ntn.repositories.PaymentOrderRepository;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PaymentOrderRepositoryImpl extends BaseHibernateRepository implements PaymentOrderRepository {

    @Override
    public PaymentOrder save(PaymentOrder order) {
        currentSession().persist(order);
        return order;
    }

    @Override
    public PaymentOrder update(PaymentOrder order) {
        return (PaymentOrder) currentSession().merge(order);
    }

    @Override
    public PaymentOrder findById(Integer orderId) {
        return currentSession().get(PaymentOrder.class, orderId);
    }

    @Override
    public PaymentOrder findByIdForUpdate(Integer orderId) {
        Session s = currentSession();
        try {
            return s.createQuery(
                    "select o from PaymentOrder o join fetch o.user where o.orderId = :orderId",
                    PaymentOrder.class)
                    .setParameter("orderId", orderId)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public PaymentOrder findByIdempotencyKey(String idempotencyKey) {
        Session s = currentSession();
        try {
            return s.createQuery(
                    "select o from PaymentOrder o join fetch o.user where o.idempotencyKey = :key",
                    PaymentOrder.class)
                    .setParameter("key", idempotencyKey)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<PaymentOrder> findByUserAndStatusIn(Integer userId, List<String> statuses) {
        return currentSession()
                .createQuery("""
                    select o from PaymentOrder o join fetch o.user
                    where o.user.userId = :userId and o.status in (:statuses)
                    order by o.createdAt desc, o.orderId desc
                """, PaymentOrder.class)
                .setParameter("userId", userId)
                .setParameter("statuses", statuses.stream()
                        .map(PaymentOrder.PaymentStatus::valueOf)
                        .toList())
                .getResultList();
    }

    @Override
    public List<PaymentOrder> findAllByStatus(String status, int page, int pageSize) {
        String normalized = normalizeStatus(status);
        String jpql = normalized == null
                ? "select o from PaymentOrder o join fetch o.user order by o.createdAt desc, o.orderId desc"
                : "select o from PaymentOrder o join fetch o.user where o.status = :status order by o.createdAt desc, o.orderId desc";

        var q = currentSession().createQuery(jpql, PaymentOrder.class);
        if (normalized != null) {
            q.setParameter("status", PaymentOrder.PaymentStatus.valueOf(normalized));
        }
        q.setFirstResult(Math.max(0, page - 1) * pageSize);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    @Override
    public long countByStatus(String status) {
        String normalized = normalizeStatus(status);
        String jpql = normalized == null
                ? "select count(o) from PaymentOrder o"
                : "select count(o) from PaymentOrder o where o.status = :status";

        var q = currentSession().createQuery(jpql, Long.class);
        if (normalized != null) {
            q.setParameter("status", PaymentOrder.PaymentStatus.valueOf(normalized));
        }
        return q.getSingleResult();
    }

    @Override
    public List<PaymentOrder> findExpiredOrders() {
        return currentSession()
                .createQuery("""
                    select o from PaymentOrder o join fetch o.user
                    where o.status = :status and o.expiredAt < :now
                    order by o.expiredAt asc
                """, PaymentOrder.class)
                .setParameter("status", PaymentOrder.PaymentStatus.PENDING)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return null;
        }
        return status.trim().toUpperCase();
    }
}
