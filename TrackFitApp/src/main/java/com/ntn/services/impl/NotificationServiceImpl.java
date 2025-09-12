package com.ntn.services.impl;

import com.ntn.dto.NotificationCreateDTO;
import com.ntn.dto.NotificationDTO;
import com.ntn.pojo.Notification;
import com.ntn.pojo.User;
import com.ntn.repositories.NotificationRepository;
import com.ntn.repositories.UserRepository;
import com.ntn.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private NotificationRepository repo;

    @Override
    public NotificationDTO createForUser(String username, NotificationCreateDTO req) {
        User u = mustUser(username);
        return save(u, req);
    }

    @Override
    public NotificationDTO createForUsername(String username, NotificationCreateDTO req) {
        User u = mustUser(username);
        return save(u, req);
    }

    @Override
    public NotificationDTO createForUserId(Integer userId, NotificationCreateDTO req) {
        User u = userRepo.findById(userId);
        if (u == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        return save(u, req);
    }

    @Override
    public NotificationDTO get(String username, Integer id) {
        User u = mustUser(username);
        Notification n = repo.findById(id);
        if (n == null || !n.getUserId().getUserId().equals(u.getUserId())) {
            throw new IllegalArgumentException("Thông báo không tồn tại hoặc không thuộc về bạn");
        }
        return toDTO(n);
    }

    @Override
    public Map<String, Object> listByUserPaged(String username, Integer page, Integer pageSize,
            Boolean isRead, String type, String kw) {
        User u = mustUser(username);
        if (pageSize == null || pageSize <= 0) {
            List<NotificationDTO> items = repo.findByUserIdFiltered(u.getUserId(), isRead, type, kw)
                    .stream().map(this::toDTO).collect(Collectors.toList());
            long total = items.size();
            return Map.of("page", 1, "pageSize", total, "totalPages", (total == 0 ? 0 : 1),
                    "totalElements", total, "items", items);
        }
        int p = (page == null || page < 1) ? 1 : page;
        long total = repo.countByUserId(u.getUserId(), isRead, type, kw);
        int totalPages = (int) Math.ceil(total * 1.0 / pageSize);
        if (p > totalPages && totalPages > 0) {
            p = totalPages;
        }

        List<NotificationDTO> items = repo.findByUserIdPaged(u.getUserId(), isRead, type, kw, p, pageSize)
                .stream().map(this::toDTO).collect(Collectors.toList());

        return Map.of("page", p, "pageSize", pageSize, "totalPages", totalPages,
                "totalElements", total, "items", items);
    }

    @Override
    public void markRead(String username, Integer id, boolean value) {
        User u = mustUser(username);
        Notification n = repo.findById(id);
        if (n == null || !n.getUserId().getUserId().equals(u.getUserId())) {
            throw new IllegalArgumentException("Thông báo không tồn tại hoặc không thuộc về bạn");
        }
        n.setIsRead(value);
        repo.save(n);
    }

    @Override
    public void delete(String username, Integer id) {
        User u = mustUser(username);
        Notification n = repo.findById(id);
        if (n == null || !n.getUserId().getUserId().equals(u.getUserId())) {
            throw new IllegalArgumentException("Thông báo không tồn tại hoặc không thuộc về bạn");
        }
        repo.delete(n);
    }

    @Override
    public long unreadCount(String username) {
        User u = mustUser(username);
        return repo.countUnread(u.getUserId());
    }

    @Override
    public int markAllRead(String username) {
        User u = mustUser(username);
        return repo.markAllRead(u.getUserId());
    }

    @Override
    public int cleanupReadOlderThan(String username, Date olderThan) {
        User u = mustUser(username);
        return repo.cleanupReadOlderThan(u.getUserId(), olderThan);
    }

    // ===== helpers =====
    private User mustUser(String username) {
        User u = userRepo.getUserByUsername(username);
        if (u == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng");
        }
        return u;
    }

    private String normalizeType(String t) {
        if (t == null) {
            return "system";
        }
        String v = t.trim().toLowerCase();
        return switch (v) {
            case "reminder", "advice", "system" ->
                v;
            default ->
                "system";
        };
    }

    private NotificationDTO save(User u, NotificationCreateDTO req) {
        Notification n = new Notification();
        n.setUserId(u);
        n.setMessage(req.getMessage());
        n.setType(normalizeType(req.getType()));
        n.setSource((req.getSource() == null || req.getSource().isBlank())
                ? "SYSTEM" : req.getSource().trim().toUpperCase());
        n.setSender(req.getSender() == null ? "System Bot" : req.getSender());
        n.setIsRead(false);
        n.setCreatedAt(new java.util.Date());
        n = repo.save(n);
        return toDTO(n);
    }

    private NotificationDTO toDTO(Notification n) {
        NotificationDTO d = new NotificationDTO();
        d.setNotificationId(n.getNotificationId());
        d.setMessage(n.getMessage());
        d.setType(n.getType());
        d.setSource(n.getSource());
        d.setSender(n.getSender());
        d.setIsRead(n.getIsRead());
        d.setCreatedAt(n.getCreatedAt());
        return d;
    }
}
