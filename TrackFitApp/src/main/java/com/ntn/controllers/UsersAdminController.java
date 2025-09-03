// src/main/java/com/ntn/controllers/UsersAdminController.java
package com.ntn.controllers;

import com.ntn.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin
public class UsersAdminController {

    @Autowired private UserRepository userRepo;

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(value = "kw", required = false) String kw,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        String needle = (kw == null ? "" : kw.trim().toLowerCase());
        List items = userRepo.findAll().stream()
                .filter(u -> {
                    if (needle.isBlank()) return true;
                    String un = Optional.ofNullable(u.getUsername()).orElse("").toLowerCase();
                    String fn = Optional.ofNullable(u.getFirstName()).orElse("").toLowerCase();
                    String ln = Optional.ofNullable(u.getLastName()).orElse("").toLowerCase();
                    String full = (fn + " " + ln).trim();
                    return un.contains(needle) || fn.contains(needle) || ln.contains(needle) || full.contains(needle);
                })
                .limit(Math.max(1, limit))
                .map(u -> Map.of(
                        "userId",     u.getUserId(),
                        "username",   u.getUsername(),
                        "fullName",   ((Optional.ofNullable(u.getFirstName()).orElse("")) + " " +
                                       (Optional.ofNullable(u.getLastName()).orElse(""))).trim(),
                        "role",       Optional.ofNullable(u.getRole()).orElse("USER")
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("items", items));
    }
}
