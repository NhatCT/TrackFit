package com.ntn.pojo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "notification")
@NamedQueries({
    @NamedQuery(name = "Notification.findAll", query = "SELECT n FROM Notification n")
})
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "notification_id")
    private Integer notificationId;

    @Basic(optional = false)
    @Lob
    @Column(name = "message")
    private String message;

    @Column(name = "type", length = 8)
    private String type;

    @Column(name = "source", length = 32)
    private String source;

    @Column(name = "sender", length = 255)
    private String sender;

    @Column(name = "is_read")
    private Boolean isRead;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false)
    private User userId;

    public Notification() {
    }

    public Notification(Integer id, String message) {
        this.notificationId = id;
        this.message = message;
    }

    // getters/setters
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

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        return notificationId != null ? notificationId.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Notification other)) {
            return false;
        }
        return (this.notificationId != null) && this.notificationId.equals(other.notificationId);
    }

    @Override
    public String toString() {
        return "Notification[id=" + notificationId + "]";
    }
}
