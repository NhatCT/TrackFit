package com.ntn.controllers;

import com.ntn.dto.NotificationCreateDTO;
import com.ntn.services.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody NotificationCreateDTO req, Principal principal) {
        return ResponseEntity.ok(service.createForUser(principal.getName(), req));
    }

    @GetMapping
    public ResponseEntity<?> list(Principal principal,
                                  @RequestParam(value="page", required=false) Integer page,
                                  @RequestParam(value="pageSize", required=false) Integer pageSize,
                                  @RequestParam(value="isRead", required=false) Boolean isRead,
                                  @RequestParam(value="type", required=false) String type,
                                  @RequestParam(value="kw", required=false) String kw) {
        return ResponseEntity.ok(service.listByUserPaged(principal.getName(), page, pageSize, isRead, type, kw));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") Integer id, Principal principal) {
        return ResponseEntity.ok(service.get(principal.getName(), id));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable("id") Integer id,
                                      @RequestParam("value") boolean value,
                                      Principal principal) {
        service.markRead(principal.getName(), id, value);
        return ResponseEntity.ok(Map.of("message", value ? "Đã đánh dấu đã đọc" : "Đã đánh dấu chưa đọc"));
    }

    // ✨ mới
    @GetMapping("/unread-count")
    public ResponseEntity<?> unread(Principal principal) {
        long c = service.unreadCount(principal.getName());
        return ResponseEntity.ok(Map.of("count", c));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> readAll(Principal principal) {
        int affected = service.markAllRead(principal.getName());
        return ResponseEntity.ok(Map.of("affected", affected));
    }

    @DeleteMapping("/cleanup")
    public ResponseEntity<?> cleanup(Principal principal,
                                     @RequestParam("olderThanDays") int olderThanDays) {
        LocalDate d = LocalDate.now(VN).minusDays(Math.max(olderThanDays, 1));
        Date dt = Date.from(d.atStartOfDay(VN).toInstant());
        int removed = service.cleanupReadOlderThan(principal.getName(), dt);
        return ResponseEntity.ok(Map.of("removed", removed));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Integer id, Principal principal) {
        service.delete(principal.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Xóa thông báo thành công"));
    }
}
