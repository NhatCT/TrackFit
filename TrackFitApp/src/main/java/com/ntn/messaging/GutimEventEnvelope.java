package com.ntn.messaging;

import java.util.Map;

/** Envelope chuẩn cho mọi message trên Kafka topic gutim.events */
public class GutimEventEnvelope {

    private String type;
    private String username;
    private Map<String, Object> payload;

    public GutimEventEnvelope() {}

    public GutimEventEnvelope(String type, String username, Map<String, Object> payload) {
        this.type = type;
        this.username = username;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
