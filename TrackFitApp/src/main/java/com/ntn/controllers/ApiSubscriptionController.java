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
public class ApiSubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping("/status")
    public ResponseEntity<SubscriptionStatusDTO> status(Principal principal) {
        return ResponseEntity.ok(subscriptionService.getStatus(principal.getName()));
    }

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

    @GetMapping("/orders/current")
    public ResponseEntity<?> getCurrentOrder(Principal principal) {
        PaymentOrderDTO order = subscriptionService.getCurrentOrder(principal.getName());
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("order", order);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmLegacy(
            @RequestBody Map<String, String> body,
            Principal principal
    ) {
        String planKey = body.getOrDefault("planKey", "monthly");
        try {
            PaymentOrderDTO order = subscriptionService.createOrder(principal.getName(), planKey);
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
