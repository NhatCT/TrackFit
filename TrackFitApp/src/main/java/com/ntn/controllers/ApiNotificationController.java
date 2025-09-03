package com.ntn.controllers;

import com.ntn.dto.NotificationCreateDTO;
import com.ntn.services.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/secure/notifications")
@CrossOrigin
public class ApiNotificationController {

    @Autowired
    private NotificationService service;

    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");

    private String getUsername(Principal principal) {
        if (principal != null) return principal.getName();
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody NotificationCreateDTO req, Principal principal) {
        String username = getUsername(principal);
        return ResponseEntity.ok(service.createForUser(username, req));
    }

    @GetMapping
    public ResponseEntity<?> list(Principal principal,
                                  @RequestParam(value="page", required=false) Integer page,
                                  @RequestParam(value="pageSize", required=false) Integer pageSize,
                                  @RequestParam(value="isRead", required=false) Boolean isRead,
                                  @RequestParam(value="type", required=false) String type,
                                  @RequestParam(value="kw", required=false) String kw) {
        String username = getUsername(principal);
        return ResponseEntity.ok(service.listByUserPaged(username, page, pageSize, isRead, type, kw));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Integer id, Principal principal) {
        String username = getUsername(principal);
        return ResponseEntity.ok(service.get(username, id));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable("id") Integer id,
                                      @RequestParam("value") boolean value,
                                      Principal principal) {
        String username = getUsername(principal);
        service.markRead(username, id, value);
        return ResponseEntity.ok(Map.of("message", value ? "Đã đánh dấu đã đọc" : "Đã đánh dấu chưa đọc"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> unread(Principal principal) {
        String username = getUsername(principal);
        long c = service.unreadCount(username);
        return ResponseEntity.ok(Map.of("count", c));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> readAll(Principal principal) {
        String username = getUsername(principal);
        int affected = service.markAllRead(username);
        return ResponseEntity.ok(Map.of("affected", affected));
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<?> cleanup(Principal principal,
                                     @RequestParam("olderThanDays") int olderThanDays) {
        String username = getUsername(principal);
        LocalDate d = LocalDate.now(VN).minusDays(Math.max(olderThanDays, 1));
        Date dt = Date.from(d.atStartOfDay(VN).toInstant());
        int removed = service.cleanupReadOlderThan(username, dt);
        return ResponseEntity.ok(Map.of("removed", removed));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Integer id, Principal principal) {
        String username = getUsername(principal);
        service.delete(username, id);
        return ResponseEntity.ok(Map.of("message", "Xóa thông báo thành công"));
    }
}
