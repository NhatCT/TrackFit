package com.ntn.controllers;

import com.ntn.dto.CreateOrderDTO;
import com.ntn.dto.PaymentOrderDTO;
import com.ntn.dto.SubscriptionStatusDTO;
import com.ntn.services.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/secure/subscription")
@CrossOrigin
public class ApiSubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping("/status")
    public ResponseEntity<SubscriptionStatusDTO> status(Principal principal) {
        return ResponseEntity.ok(subscriptionService.getStatus(principal.getName()));
    }

    /**
     * Tạo đơn hàng thanh toán (idempotent).
     * POST /api/secure/subscription/create-order
     * Body: { "planKey": "monthly" }
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody CreateOrderDTO body,
            Principal principal
    ) {
        String planKey = body.getPlanKey();
        if (planKey == null || planKey.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn gói đăng ký."));
        }
        try {
            PaymentOrderDTO order = subscriptionService.createOrder(principal.getName(), planKey);
            return ResponseEntity.ok(Map.of("order", order));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * User xác nhận đã chuyển khoản: PENDING → SUBMITTED.
     * PUT /api/secure/subscription/orders/{id}/submitted
     */
    @PutMapping("/orders/{id}/submitted")
    public ResponseEntity<?> submitOrder(
            @PathVariable("id") int orderId,
            Principal principal
    ) {
        try {
            PaymentOrderDTO order = subscriptionService.submitOrder(principal.getName(), orderId);
            return ResponseEntity.ok(Map.of(
                    "message", "Đã xác nhận chuyển khoản. Đang chờ Admin xác minh.",
                    "order", order
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Lấy đơn hàng hiện tại (PENDING/SUBMITTED) của user.
     * GET /api/secure/subscription/orders/current
     */
    @GetMapping("/orders/current")
    public ResponseEntity<?> getCurrentOrder(Principal principal) {
        PaymentOrderDTO order = subscriptionService.getCurrentOrder(principal.getName());
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("order", order);
        return ResponseEntity.ok(response);
    }

    /**
     * Legacy confirm endpoint — giữ tương thích ngược.
     * POST /api/secure/subscription/confirm
     * Chuyển hướng sang createOrder + submitOrder.
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmLegacy(
            @RequestBody Map<String, String> body,
            Principal principal
    ) {
        String planKey = body.getOrDefault("planKey", "monthly");
        try {
            PaymentOrderDTO order = subscriptionService.createOrder(principal.getName(), planKey);
            // Auto-submit cho legacy flow
            if ("PENDING".equals(order.getStatus())) {
                order = subscriptionService.submitOrder(principal.getName(), order.getOrderId());
            }
            return ResponseEntity.ok(Map.of(
                    "message", "Đơn hàng đã được tạo và đang chờ xác minh.",
                    "order", order
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
