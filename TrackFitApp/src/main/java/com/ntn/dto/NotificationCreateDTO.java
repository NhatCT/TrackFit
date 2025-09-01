package com.ntn.dto;

import jakarta.validation.constraints.*;

public class NotificationCreateDTO {

    @NotBlank
    @Size(max = 65535)
    private String message;

    @NotBlank
    @Size(max = 8)
    @Pattern(regexp = "^(ADVICE|SYSTEM|REMINDER)$",
            message = "type phải là ADVICE, SYSTEM hoặc REMINDER")
    private String type;

    @Size(max = 32)
    @Pattern(regexp = "^(SYSTEM|AI|ADMIN|USER|EXTERNAL)?$",
            message = "source phải là SYSTEM, AI, ADMIN, USER hoặc EXTERNAL")
    private String source; // optional

    @Size(max = 255)
    private String sender; // optional

    // getters/setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
