package com.ntn.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntn.dto.NotificationDTO;
import com.ntn.dto.WebSocketEventDTO;
import com.ntn.messaging.GutimEventEnvelope;
import com.ntn.services.WebSocketEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketEventServiceImpl implements WebSocketEventService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.enabled:false}")
    private boolean kafkaEnabled;

    @Override
    public void pushToUser(String username, WebSocketEventDTO event) {
        if (event.getAt() == null) {
            event.setAt(LocalDateTime.now());
        }
        try {
            messagingTemplate.convertAndSendToUser(username, "/queue/events", event);
        } catch (Exception e) {
            System.err.println("[WebSocket] push events failed: " + e.getMessage());
        }
    }

    @Override
    public void publishNotificationCreated(String username, NotificationDTO notification) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("notificationId", notification.getNotificationId());
        payload.put("message", notification.getMessage());
        payload.put("type", notification.getType());
        payload.put("source", notification.getSource());
        payload.put("sender", notification.getSender());
        payload.put("isRead", notification.getIsRead());
        payload.put("createdAt", notification.getCreatedAt());

        publishOrDirect(
                GutimEventTopics.TYPE_NOTIFICATION_CREATED,
                username,
                payload,
                () -> messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notification)
        );
    }

    @Override
    public void publishSubscriptionActivated(String username, Map<String, Object> data) {
        publishOrDirect(
                GutimEventTopics.TYPE_SUBSCRIPTION_ACTIVATED,
                username,
                data,
                () -> {
                    WebSocketEventDTO evt = new WebSocketEventDTO();
                    evt.setType(GutimEventTopics.TYPE_SUBSCRIPTION_ACTIVATED);
                    evt.setTitle("GUTIM PRO đã kích hoạt");
                    evt.setMessage("Chúc mừng! Tài khoản của bạn đã được nâng cấp PRO.");
                    evt.setData(data);
                    evt.setAt(LocalDateTime.now());
                    pushToUser(username, evt);
                }
        );
    }

    private void publishOrDirect(String type, String username, Map<String, Object> payload, Runnable directFallback) {
        if (kafkaEnabled && kafkaTemplate != null) {
            try {
                GutimEventEnvelope env = new GutimEventEnvelope(type, username, payload);
                String json = objectMapper.writeValueAsString(env);
                kafkaTemplate.send(GutimEventTopics.GUTIM_EVENTS, username, json);
                return;
            } catch (Exception e) {
                System.err.println("[Kafka] publish failed, fallback direct: " + e.getMessage());
            }
        }
        directFallback.run();
    }
}
