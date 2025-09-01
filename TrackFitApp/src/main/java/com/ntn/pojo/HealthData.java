package com.ntn.pojo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "health_data")
@NamedQueries({
    @NamedQuery(name = "HealthData.findAll", query = "SELECT h FROM HealthData h"),
    @NamedQuery(name = "HealthData.findByHealthId", query = "SELECT h FROM HealthData h WHERE h.healthId = :healthId"),
    @NamedQuery(name = "HealthData.findByUserId", query = "SELECT h FROM HealthData h WHERE h.userId.userId = :userId")
})
public class HealthData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "health_id")
    private Integer healthId;

    @Column(name = "height")           // cm
    private BigDecimal height;

    @Column(name = "weight")           // kg
    private BigDecimal weight;

    @Column(name = "blood_pressure", length = 20)   // ➕
    private String bloodPressure;

    @Column(name = "notes", length = 255)           // ➕
    private String notes;

    @Column(name = "created_at")                     // ➕
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false)
    private User userId;

    public HealthData() {
    }

    public Integer getHealthId() {
        return healthId;
    }

    public void setHealthId(Integer healthId) {
        this.healthId = healthId;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
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

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        return healthId != null ? healthId.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HealthData)) {
            return false;
        }
        HealthData other = (HealthData) obj;
        return (this.healthId != null && this.healthId.equals(other.healthId));
    }

    @Override
    public String toString() {
        return "com.ntn.pojo.HealthData[ healthId=" + healthId + " ]";
    }

}
