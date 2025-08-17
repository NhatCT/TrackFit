// src/main/java/com/ntn/controllers/ApiUserWorkoutHistoryController.java
package com.ntn.controllers;

import com.ntn.dto.HistoryCreateUpdateDTO;
import com.ntn.services.UserWorkoutHistoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/secure/histories")
@CrossOrigin
public class ApiUserWorkoutHistoryController {

    @Autowired
    private UserWorkoutHistoryService service;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody HistoryCreateUpdateDTO req, Principal principal) {
        return ResponseEntity.ok(service.create(principal.getName(), req));
    }

    @GetMapping
    public ResponseEntity<?> list(Principal principal,
                                  @RequestParam(value="page", required=false) Integer page,
                                  @RequestParam(value="pageSize", required=false) Integer pageSize,
                                  @RequestParam(value="planId", required=false) Integer planId,
                                  @RequestParam(value="exerciseId", required=false) Integer exerciseId,
                                  @RequestParam(value="status", required=false) String status) {
        return ResponseEntity.ok(service.listByUserPaged(principal.getName(), page, pageSize, planId, exerciseId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Integer id, Principal principal) {
        return ResponseEntity.ok(service.get(principal.getName(), id));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable("id") Integer id,
                                    @Valid @RequestBody HistoryCreateUpdateDTO req,
                                    Principal principal) {
        return ResponseEntity.ok(service.update(principal.getName(), id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Integer id, Principal principal) {
        service.delete(principal.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Xóa lịch sử thành công"));
    }
}
