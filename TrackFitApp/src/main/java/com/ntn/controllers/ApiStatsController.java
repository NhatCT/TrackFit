package com.ntn.controllers;

import com.ntn.dto.StatsSummaryDTO;
import com.ntn.services.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Date;

@RestController
@RequestMapping("/api/secure/stats")
@CrossOrigin
public class ApiStatsController {

    @Autowired private StatsService statsService;

    @GetMapping("/summary")
    public ResponseEntity<?> summary(
            Principal principal,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate from,
            @RequestParam(value = "to", required = false)   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate to
    ) {
        Date f = from != null ? java.util.Date.from(from.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()) : null;
        Date t = to   != null ? java.util.Date.from(to.atTime(23,59,59).atZone(java.time.ZoneId.systemDefault()).toInstant()) : null;
        StatsSummaryDTO dto = statsService.summary(principal.getName(), f, t);
        return ResponseEntity.ok(dto);
    }
}
