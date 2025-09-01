package com.ntn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class NotificationDTO {

    private Integer notificationId;
    private String message;   // nội dung gốc
    private String type;      // ADVICE | SYSTEM | REMINDER
    private String source;    // SYSTEM | AI | ADMIN | USER | EXTERNAL
    private String sender;    // "System Bot", "AI Coach"...
    private Boolean isRead;
    private Date createdAt;

    // ====== Các alias để FE cũ dùng được (title/content/read) ======
    @JsonProperty("title")
    public String getTitle() {
        // có thể tùy biến hiển thị tiêu đề theo type/source
        return switch (type == null ? "" : type.toUpperCase()) {
            case "ADVICE" ->
                "Lời khuyên";
            case "REMINDER" ->
                "Nhắc nhở";
            case "SYSTEM" ->
                "Hệ thống";
            default ->
                "Thông báo";
        };
    }

    @JsonProperty("content")
    public String getContent() {
        return message;
    }

    @JsonProperty("read")
    public Boolean getRead() {
        return isRead;
    }

    // ====== getters/setters gốc ======
    public Integer getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }

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

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
