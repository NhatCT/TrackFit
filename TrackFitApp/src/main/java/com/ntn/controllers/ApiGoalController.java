package com.ntn.controllers;

import com.ntn.dto.GoalDTO;
import com.ntn.services.GoalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/secure/goals")
@CrossOrigin
public class ApiGoalController {

    @Autowired
    private GoalService goalService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody GoalDTO dto, Principal principal) {
        goalService.create(principal.getName(), dto);
        return ResponseEntity.ok(java.util.Map.of("message", "Thêm mục tiêu thành công"));
    }

    @GetMapping
    public ResponseEntity<?> list(Principal principal) {
        return ResponseEntity.ok(goalService.listByUsername(principal.getName()));
    }

    @PutMapping(path = "/{goalId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable("goalId") Integer goalId,
                                    @Valid @RequestBody GoalDTO dto,
                                    Principal principal) {
        goalService.update(principal.getName(), goalId, dto);
        return ResponseEntity.ok(java.util.Map.of("message", "Cập nhật mục tiêu thành công"));
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<?> delete(@PathVariable("goalId") Integer goalId, Principal principal) {
        goalService.delete(principal.getName(), goalId);
        return ResponseEntity.ok(java.util.Map.of("message", "Xóa mục tiêu thành công"));
    }
}
