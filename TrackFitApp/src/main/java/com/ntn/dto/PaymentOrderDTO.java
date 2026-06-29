package com.ntn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ntn.pojo.PaymentOrder;
import java.time.LocalDateTime;

public class PaymentOrderDTO {

    private Integer orderId;
    private Integer userId;
    private String username;
    private String planKey;
    private Integer amount;
    private String status;
    private String transferRef;
    private String adminNote;
    private String verifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submittedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime verifiedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiredAt;

    public static PaymentOrderDTO fromEntity(PaymentOrder order) {
        if (order == null) {
            return null;
        }
        PaymentOrderDTO dto = new PaymentOrderDTO();
        dto.setOrderId(order.getOrderId());
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getUserId());
            dto.setUsername(order.getUser().getUsername());
        }
        dto.setPlanKey(order.getPlanKey());
        dto.setAmount(order.getAmount());
        dto.setStatus(order.getStatus() == null ? null : order.getStatus().name());
        dto.setTransferRef(order.getTransferRef());
        dto.setAdminNote(order.getAdminNote());
        dto.setVerifiedBy(order.getVerifiedBy());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setSubmittedAt(order.getSubmittedAt());
        dto.setVerifiedAt(order.getVerifiedAt());
        dto.setExpiredAt(order.getExpiredAt());
        return dto;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPlanKey() {
        return planKey;
    }

    public void setPlanKey(String planKey) {
        this.planKey = planKey;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransferRef() {
        return transferRef;
    }

    public void setTransferRef(String transferRef) {
        this.transferRef = transferRef;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }
}
