package com.ntn.controllers;

import com.ntn.dto.StatsSummaryDTO;
import com.ntn.services.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("/api/secure/stats")
@CrossOrigin
public class ApiStatsController {

    @Autowired private StatsService statsService;

    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");

@GetMapping("/summary")
public ResponseEntity<StatsSummaryDTO> summary(
        Principal principal,
        @RequestParam(value = "from", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(value = "to", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

    if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    Date f = (from != null) ? Date.from(from.atStartOfDay(VN).toInstant()) : null;
    Date t = (to   != null) ? Date.from(to.plusDays(1).atStartOfDay(VN).toInstant()) : null;

    return ResponseEntity.ok(statsService.summaryUser(principal.getName(), f, t));
}
}