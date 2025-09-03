package com.ntn.controllers;

import com.ntn.dto.RecommendationParamsDTO;
import com.ntn.repositories.UserRepository;
import com.ntn.services.AiAdviceService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// com/ntn/controllers/NotificationsAiAdminController.java
@RestController
@RequestMapping("/api/admin/notifications/ai")
@CrossOrigin
public class NotificationsAiAdminController {

    @Autowired private AiAdviceService adviceService;
    @Autowired private UserRepository userRepo;

    // Tạo cho 1 user cụ thể
    @PostMapping("/for-user")
    public ResponseEntity<?> forUser(
            @RequestParam("username") String username,
            @RequestParam(value = "top", defaultValue = "3") int top,
            @RequestParam(value = "withinDays", defaultValue = "1") int withinDays,
            @RequestBody(required = false) RecommendationParamsDTO params) {
        int created = adviceService.sendAdviceFromRecoIfNotExists(username, params, top, withinDays);
        return ResponseEntity.ok(Map.of("created", created));
    }

    // Chạy cho tất cả user
    @PostMapping("/run-now")
    public ResponseEntity<?> runNow(
            @RequestParam(value = "top", defaultValue = "3") int top,
            @RequestParam(value = "withinDays", defaultValue = "1") int withinDays,
            @RequestBody(required = false) RecommendationParamsDTO params) {
        int total = 0;
        for (var u : userRepo.findAll()) {
            total += adviceService.sendAdviceFromRecoIfNotExists(u.getUsername(), params, top, withinDays);
        }
        return ResponseEntity.ok(Map.of("created", total));
    }
}


