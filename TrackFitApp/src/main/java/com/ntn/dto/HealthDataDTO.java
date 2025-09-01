package com.ntn.dto;

import java.util.Date;
import java.time.LocalDate;

public class HealthDataDTO {

    // FE cần để hiển thị/xóa
    private Integer healthId;

    private Double height;          // cm
    private Double weight;          // kg
    private String bloodPressure;   // "120/80"
    private String notes;

    private Date createdAt;
    private Date updatedAt;

    // Tham khảo từ bảng user (tuỳ form)
    private String gender;
    private LocalDate birthDate;

    public Integer getHealthId() {
        return healthId;
    }
    public void setHealthId(Integer healthId) {
        this.healthId = healthId;
    }

    public Double getHeight() {
        return height;
    }
    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }
    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }
    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
