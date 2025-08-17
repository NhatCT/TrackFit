package com.ntn.controllers;

import com.ntn.dto.HealthDataDTO;
import com.ntn.services.HealthDataService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/secure/health")
@CrossOrigin
public class ApiHealthController {

    @Autowired
    private HealthDataService healthDataService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody HealthDataDTO dto, Principal principal) {
        healthDataService.create(principal.getName(), dto);
        return ResponseEntity.ok(java.util.Map.of("message", "Thêm thông tin sức khỏe thành công"));
    }

    @GetMapping
    public ResponseEntity<?> list(Principal principal) {
        return ResponseEntity.ok(healthDataService.listByUsername(principal.getName()));
    }

    @PutMapping(path = "/{healthId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable("healthId") Integer healthId,
                                    @Valid @RequestBody HealthDataDTO dto,
                                    Principal principal) {
        healthDataService.update(principal.getName(), healthId, dto);
        return ResponseEntity.ok(java.util.Map.of("message", "Cập nhật thông tin sức khỏe thành công"));
    }

    @DeleteMapping("/{healthId}")
    public ResponseEntity<?> delete(@PathVariable("healthId") Integer healthId, Principal principal) {
        healthDataService.delete(principal.getName(), healthId);
        return ResponseEntity.ok(java.util.Map.of("message", "Xóa thông tin sức khỏe thành công"));
    }
}
