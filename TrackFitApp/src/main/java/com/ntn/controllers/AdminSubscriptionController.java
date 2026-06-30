package com.ntn.controllers;

import com.ntn.dto.AdminVerifyOrderDTO;
import com.ntn.dto.PaymentOrderDTO;
import com.ntn.services.SubscriptionService;
import java.security.Principal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/subscription")
public class AdminSubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping("/orders")
    public ResponseEntity<?> listOrders(
            @RequestParam(value = "status", defaultValue = "SUBMITTED") String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize
    ) {
        return ResponseEntity.ok(Map.of(
                "orders", subscriptionService.listOrders(status, page, pageSize),
                "total", subscriptionService.countOrders(status),
                "page", page,
                "pageSize", pageSize
        ));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrder(@PathVariable("id") int orderId) {
        PaymentOrderDTO order = subscriptionService.getOrder(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("order", order));
    }

    @PutMapping("/orders/{id}/verify")
    public ResponseEntity<?> verifyOrder(
            @PathVariable("id") int orderId,
            @RequestBody AdminVerifyOrderDTO body,
            Principal principal
    ) {
        try {
            PaymentOrderDTO order = subscriptionService.verifyOrder(
                    principal.getName(),
                    orderId,
                    body.isApproved(),
                    body.getNote()
            );
            return ResponseEntity.ok(Map.of(
                    "message", body.isApproved() ? "Đã kích hoạt PRO." : "Đã từ chối đơn hàng.",
                    "order", order
            ));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
