package com.ntn.services;

import com.ntn.dto.NotificationCreateDTO;
import com.ntn.dto.NotificationDTO;

import java.util.Map;

public interface NotificationService {
    NotificationDTO createForUser(String username, NotificationCreateDTO req);
    NotificationDTO get(String username, Integer id);
    Map<String, Object> listByUserPaged(String username, Integer page, Integer pageSize,
                                        Boolean isRead, String type, String kw);
    void markRead(String username, Integer id, boolean value);
    void delete(String username, Integer id);
}
