package com.ntn.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntn.dto.NotificationDTO;
import com.ntn.dto.WebSocketEventDTO;
import com.ntn.messaging.GutimEventEnvelope;
import com.ntn.messaging.GutimEventTopics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

/**
 * Consumer Kafka → đẩy real-time qua WebSocket/STOMP.
 * Luồng: Service lưu DB → publish Kafka → consumer này → /user/queue/*
 */
@Component
public class GutimEventConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.enabled:false}")
    private boolean kafkaEnabled;

    @KafkaListener(
            topics = GutimEventTopics.GUTIM_EVENTS,
            groupId = "${kafka.consumer.group-id:gutim-ws-bridge}",
            autoStartup = "${kafka.enabled:false}"
    )
    public void onGutimEvent(String raw) {
        if (!kafkaEnabled) {
            return;
        }
        try {
            GutimEventEnvelope env = objectMapper.readValue(raw, GutimEventEnvelope.class);
            if (env.getUsername() == null || env.getType() == null) {
                return;
            }
            switch (env.getType()) {
                case GutimEventTopics.TYPE_NOTIFICATION_CREATED -> deliverNotification(env);
                case GutimEventTopics.TYPE_SUBSCRIPTION_ACTIVATED -> deliverSubscription(env);
                default -> System.out.println("[Kafka] Unknown event type: " + env.getType());
            }
        } catch (Exception e) {
            System.err.println("[Kafka] consume error: " + e.getMessage());
        }
    }

    private void deliverNotification(GutimEventEnvelope env) {
        Map<String, Object> p = env.getPayload();
        if (p == null) {
            return;
        }
        NotificationDTO dto = new NotificationDTO();
        if (p.get("notificationId") != null) {
            dto.setNotificationId(((Number) p.get("notificationId")).intValue());
        }
        dto.setMessage((String) p.get("message"));
        dto.setType((String) p.get("type"));
        dto.setSource((String) p.get("source"));
        dto.setSender((String) p.get("sender"));
        if (p.get("isRead") != null) {
            dto.setIsRead((Boolean) p.get("isRead"));
        }
        Object created = p.get("createdAt");
        if (created instanceof Date d) {
            dto.setCreatedAt(d);
        } else if (created instanceof String s) {
            try {
                dto.setCreatedAt(java.sql.Timestamp.valueOf(s.replace("T", " ").substring(0, 19)));
            } catch (Exception ignored) {}
        }
        messagingTemplate.convertAndSendToUser(env.getUsername(), "/queue/notifications", dto);
    }

    private void deliverSubscription(GutimEventEnvelope env) {
        WebSocketEventDTO evt = new WebSocketEventDTO();
        evt.setType(GutimEventTopics.TYPE_SUBSCRIPTION_ACTIVATED);
        evt.setTitle("GUTIM PRO đã kích hoạt");
        evt.setMessage("Chúc mừng! Tài khoản của bạn đã được nâng cấp PRO.");
        evt.setData(env.getPayload());
        evt.setAt(LocalDateTime.now());
        messagingTemplate.convertAndSendToUser(env.getUsername(), "/queue/events", evt);
    }
}
