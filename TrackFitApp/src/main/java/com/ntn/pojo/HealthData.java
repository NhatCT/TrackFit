/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.pojo;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Thanh Nhat
 */
@Entity
@Table(name = "health_data")
@NamedQueries({
    @NamedQuery(name = "HealthData.findAll", query = "SELECT h FROM HealthData h"),
    @NamedQuery(name = "HealthData.findByHealthId", query = "SELECT h FROM HealthData h WHERE h.healthId = :healthId"),
    @NamedQuery(name = "HealthData.findByHeight", query = "SELECT h FROM HealthData h WHERE h.height = :height"),
    @NamedQuery(name = "HealthData.findByWeight", query = "SELECT h FROM HealthData h WHERE h.weight = :weight"),
    @NamedQuery(name = "HealthData.findByUpdatedAt", query = "SELECT h FROM HealthData h WHERE h.updatedAt = :updatedAt"),
    @NamedQuery(name = "HealthData.findByUserId",
            query = "SELECT h FROM HealthData h WHERE h.userId.userId = :userId ORDER BY h.updatedAt DESC")}
)
public class HealthData implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "health_id")
    private Integer healthId;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "height")
    private BigDecimal height;
    @Column(name = "weight")
    private BigDecimal weight;
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false)
    private User userId;

    public HealthData() {
    }

    public HealthData(Integer healthId) {
        this.healthId = healthId;
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
        int hash = 0;
        hash += (healthId != null ? healthId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof HealthData)) {
            return false;
        }
        HealthData other = (HealthData) object;
        if ((this.healthId == null && other.healthId != null) || (this.healthId != null && !this.healthId.equals(other.healthId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ntn.pojo.HealthData[ healthId=" + healthId + " ]";
    }

}
