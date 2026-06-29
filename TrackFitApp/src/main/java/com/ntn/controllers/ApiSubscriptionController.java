package com.ntn.controllers;

import com.ntn.dto.SubscriptionConfirmDTO;
import com.ntn.dto.SubscriptionStatusDTO;
import com.ntn.dto.UserResponseDTO;
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
     * Xác nhận thanh toán (demo): kích hoạt PRO trên DB + publish Kafka → WebSocket.
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(
            @Valid @RequestBody SubscriptionConfirmDTO body,
            Principal principal
    ) {
        UserResponseDTO user = subscriptionService.confirmPayment(principal.getName(), body);
        return ResponseEntity.ok(Map.of(
                "message", "Nâng cấp GUTIM PRO thành công",
                "user", user
        ));
    }
}
