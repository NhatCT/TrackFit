package com.ntn.controllers;

import com.ntn.dto.*;
import com.ntn.services.WorkoutPlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/secure/plans")
@CrossOrigin
public class ApiWorkoutPlanController {

    @Autowired
    private WorkoutPlanService workoutPlanService;

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equalsIgnoreCase(a.getAuthority()));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody WorkoutPlanCreateRequest req, Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean admin = isAdmin(auth);
        if (!admin && req.getUserId() != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Bạn không có quyền tạo kế hoạch cho người khác"));
        }
        if (!admin) {
            req.setIsTemplate(Boolean.FALSE);
        }

        WorkoutPlanResponseDTO dto = workoutPlanService.createPlan(principal.getName(), req);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<?> get(@PathVariable("planId") Integer planId) {
        return ResponseEntity.ok(workoutPlanService.getPlan(planId));
    }

    @GetMapping
    public ResponseEntity<?> listByUser(Principal principal,
                                        @RequestParam(value = "page", required = false) Integer page,
                                        @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                        @RequestParam(value = "kw", required = false) String kw) {
        return ResponseEntity.ok(
                workoutPlanService.listPlansByUserPaged(principal.getName(), page, pageSize, kw)
        );
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<?> delete(@PathVariable("planId") Integer planId, Principal principal) {
        workoutPlanService.deletePlan(principal.getName(), planId);
        return ResponseEntity.ok(Map.of("message", "Xóa kế hoạch thành công"));
    }

    @PostMapping(path = "/{planId}/details", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addDetail(@PathVariable("planId") Integer planId,
                                       @Valid @RequestBody PlanDetailItemDTO req) {
        return ResponseEntity.ok(workoutPlanService.addDetail(planId, req));
    }

    @PutMapping(path = "/details/{detailId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateDetail(@PathVariable("detailId") Integer detailId,
                                          @Valid @RequestBody PlanDetailItemDTO req) {
        return ResponseEntity.ok(workoutPlanService.updateDetail(detailId, req));
    }

    @DeleteMapping("/details/{detailId}")
    public ResponseEntity<?> deleteDetail(@PathVariable("detailId") Integer detailId) {
        workoutPlanService.deleteDetail(detailId);
        return ResponseEntity.ok(Map.of("message", "Xóa chi tiết kế hoạch thành công"));
    }

    @PutMapping(path = "/{planId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePlan(@PathVariable("planId") Integer planId,
                                        @RequestBody WorkoutPlanCreateRequest req,
                                        Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean admin = isAdmin(auth);
        if (!admin) {
            req.setIsTemplate(null);
        }
        if (admin) {
            return ResponseEntity.ok(workoutPlanService.updatePlanAdmin(planId, req));
        } else {
            return ResponseEntity.ok(workoutPlanService.updatePlanAdmin(planId, req));
        }
    }
}
