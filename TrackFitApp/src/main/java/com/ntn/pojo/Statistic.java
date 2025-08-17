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
@Table(name = "statistic")
@NamedQueries({
    @NamedQuery(name = "Statistic.findAll", query = "SELECT s FROM Statistic s"),
    @NamedQuery(name = "Statistic.findByStatId", query = "SELECT s FROM Statistic s WHERE s.statId = :statId"),
    @NamedQuery(name = "Statistic.findByStatType", query = "SELECT s FROM Statistic s WHERE s.statType = :statType"),
    @NamedQuery(name = "Statistic.findByValue", query = "SELECT s FROM Statistic s WHERE s.value = :value"),
    @NamedQuery(name = "Statistic.findByGeneratedAt", query = "SELECT s FROM Statistic s WHERE s.generatedAt = :generatedAt")})
public class Statistic implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "stat_id")
    private Integer statId;
    @jakarta.validation.constraints.Size(max = 50)
    @Column(name = "stat_type")
    private String statType;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "value")
    private BigDecimal value;
    @Column(name = "generated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date generatedAt;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne
    private User userId;

    public Statistic() {
    }

    public Statistic(Integer statId) {
        this.statId = statId;
    }

    public Integer getStatId() {
        return statId;
    }

    public void setStatId(Integer statId) {
        this.statId = statId;
    }

    public String getStatType() {
        return statType;
    }

    public void setStatType(String statType) {
        this.statType = statType;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Date getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Date generatedAt) {
        this.generatedAt = generatedAt;
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
        hash += (statId != null ? statId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Statistic)) {
            return false;
        }
        Statistic other = (Statistic) object;
        if ((this.statId == null && other.statId != null) || (this.statId != null && !this.statId.equals(other.statId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ntn.pojo.Statistic[ statId=" + statId + " ]";
    }
    
}
