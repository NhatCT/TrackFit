package com.ntn.repositories;

import com.ntn.pojo.PaymentOrder;
import java.util.List;

public interface PaymentOrderRepository {

    PaymentOrder save(PaymentOrder order);

    PaymentOrder update(PaymentOrder order);

    PaymentOrder findById(Integer orderId);

    PaymentOrder findByIdForUpdate(Integer orderId);

    PaymentOrder findByIdempotencyKey(String idempotencyKey);

    List<PaymentOrder> findByUserAndStatusIn(Integer userId, List<String> statuses);

    List<PaymentOrder> findAllByStatus(String status, int page, int pageSize);

    long countByStatus(String status);

    List<PaymentOrder> findExpiredOrders();
}
