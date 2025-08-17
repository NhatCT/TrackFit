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
import java.io.Serializable;

/**
 *
 * @author Thanh Nhat
 */
@Entity
@Table(name = "plan_detail")
@NamedQueries({
    @NamedQuery(name = "PlanDetail.findAll", query = "SELECT p FROM PlanDetail p"),
    @NamedQuery(name = "PlanDetail.findByDetailId", query = "SELECT p FROM PlanDetail p WHERE p.detailId = :detailId"),
    @NamedQuery(name = "PlanDetail.findByDayOfWeek", query = "SELECT p FROM PlanDetail p WHERE p.dayOfWeek = :dayOfWeek"),
    @NamedQuery(name = "PlanDetail.findByDuration", query = "SELECT p FROM PlanDetail p WHERE p.duration = :duration"),
    @NamedQuery(name = "PlanDetail.findByPlanId",
            query = "SELECT p FROM PlanDetail p WHERE p.planId.planId = :planId ORDER BY p.dayOfWeek ASC, p.detailId ASC"),
    @NamedQuery(name = "PlanDetail.findByPlanIdAndDay",
            query = "SELECT p FROM PlanDetail p WHERE p.planId.planId = :planId AND p.dayOfWeek = :dayOfWeek ORDER BY p.detailId ASC")
})
public class PlanDetail implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "detail_id")
    private Integer detailId;
    @Column(name = "day_of_week")
    private Integer dayOfWeek;
    @Column(name = "duration")
    private Integer duration;
    @JoinColumn(name = "exercises_id", referencedColumnName = "exercises_id")
    @ManyToOne(optional = false)
    private Exercises exercisesId;
    @JoinColumn(name = "plan_id", referencedColumnName = "plan_id")
    @ManyToOne(optional = false)
    private WorkoutPlan planId;

    public PlanDetail() {
    }

    public PlanDetail(Integer detailId) {
        this.detailId = detailId;
    }

    public Integer getDetailId() {
        return detailId;
    }

    public void setDetailId(Integer detailId) {
        this.detailId = detailId;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Exercises getExercisesId() {
        return exercisesId;
    }

    public void setExercisesId(Exercises exercisesId) {
        this.exercisesId = exercisesId;
    }

    public WorkoutPlan getPlanId() {
        return planId;
    }

    public void setPlanId(WorkoutPlan planId) {
        this.planId = planId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (detailId != null ? detailId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PlanDetail)) {
            return false;
        }
        PlanDetail other = (PlanDetail) object;
        if ((this.detailId == null && other.detailId != null) || (this.detailId != null && !this.detailId.equals(other.detailId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ntn.pojo.PlanDetail[ detailId=" + detailId + " ]";
    }

}
