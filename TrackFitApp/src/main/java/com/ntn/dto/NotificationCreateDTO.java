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
}
