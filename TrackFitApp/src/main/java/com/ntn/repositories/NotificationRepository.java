package com.ntn.repositories;

import com.ntn.pojo.Notification;

import java.util.Date;
import java.util.List;

public interface NotificationRepository {
    Notification save(Notification n);
    Notification findById(Integer id);
    void delete(Notification n);

    List<Notification> findByUserIdFiltered(Integer userId, Boolean isRead, String type, String kw);
    List<Notification> findByUserIdPaged(Integer userId, Boolean isRead, String type, String kw, int page, int pageSize);
    long countByUserId(Integer userId, Boolean isRead, String type, String kw);

    // ✨ mở rộng
    long countUnread(Integer userId);
    int markAllRead(Integer userId);
    int cleanupReadOlderThan(Integer userId, Date olderThan);
}
