package com.ntn.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // FE kết nối: http(s)://<host>/TrackFit/ws
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*"); // .withSockJS() nếu muốn fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Broker nội bộ cho subscribe
        registry.enableSimpleBroker("/topic", "/queue");
        // Tiền tố cho @MessageMapping (nếu dùng sau này)
        registry.setApplicationDestinationPrefixes("/app");
        // Cho phép /user/queue nếu dùng convertAndSendToUser (không bắt buộc)
        registry.setUserDestinationPrefix("/user");
    }
}
