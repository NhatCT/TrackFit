package com.ntn.controllers;

import com.ntn.dto.NotificationCreateDTO;
import com.ntn.services.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/secure/notifications/admin")
@CrossOrigin
public class AdminNotificationController {

    @Autowired private NotificationService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/send-to/{username}")
    public ResponseEntity<?> sendToUsername(@PathVariable String username,
                                            @Valid @RequestBody NotificationCreateDTO req) {
        return ResponseEntity.ok(service.createForUsername(username, req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/send-to-id/{userId}")
    public ResponseEntity<?> sendToUserId(@PathVariable Integer userId,
                                          @Valid @RequestBody NotificationCreateDTO req) {
        return ResponseEntity.ok(service.createForUserId(userId, req));
    }
}
