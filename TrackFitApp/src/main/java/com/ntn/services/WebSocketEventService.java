package com.ntn.services;

import com.ntn.dto.NotificationDTO;
import com.ntn.dto.WebSocketEventDTO;
import java.util.Map;

public interface WebSocketEventService {

    void pushToUser(String username, WebSocketEventDTO event);

    void publishNotificationCreated(String username, NotificationDTO notification);

    void publishSubscriptionActivated(String username, Map<String, Object> data);
}
