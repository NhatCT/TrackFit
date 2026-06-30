package com.ntn.services;

import com.ntn.dto.PaymentOrderDTO;
import com.ntn.dto.SubscriptionStatusDTO;

import java.util.List;

public interface SubscriptionService {

    SubscriptionStatusDTO getStatus(String username);

    PaymentOrderDTO createOrder(String username, String planKey);

    PaymentOrderDTO submitOrder(String username, int orderId);

    PaymentOrderDTO getCurrentOrder(String username);

    PaymentOrderDTO verifyOrder(String adminUsername, int orderId, boolean approved, String note);

    List<PaymentOrderDTO> listOrders(String status, int page, int pageSize);

    PaymentOrderDTO getOrder(int orderId);

    long countOrders(String status);

    void cancelExpiredOrders();
}
