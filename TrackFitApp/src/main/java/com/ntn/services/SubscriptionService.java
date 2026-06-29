package com.ntn.services;

import com.ntn.dto.PaymentOrderDTO;
import com.ntn.dto.SubscriptionStatusDTO;
import com.ntn.dto.UserResponseDTO;

import java.util.List;

public interface SubscriptionService {

    SubscriptionStatusDTO getStatus(String username);

    // === User-facing ===

    /** Tạo đơn hàng mới (idempotent — cùng user+plan+ngày → trả order cũ) */
    PaymentOrderDTO createOrder(String username, String planKey);

    /** User xác nhận đã chuyển khoản: PENDING → SUBMITTED */
    PaymentOrderDTO submitOrder(String username, int orderId);

    /** Lấy order hiện tại (PENDING/SUBMITTED) của user */
    PaymentOrderDTO getCurrentOrder(String username);

    // === Admin-facing ===

    /** Admin xác minh thanh toán: SUBMITTED → VERIFIED → ACTIVATED */
    PaymentOrderDTO verifyOrder(String adminUsername, int orderId, boolean approved, String note);

    /** Danh sách orders theo status (cho Admin Dashboard) */
    List<PaymentOrderDTO> listOrders(String status, int page, int pageSize);

    PaymentOrderDTO getOrder(int orderId);

    long countOrders(String status);

    /** Tự động huỷ các order hết hạn */
    void cancelExpiredOrders();
}
