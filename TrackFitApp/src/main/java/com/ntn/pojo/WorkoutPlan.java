/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ntn.pojo;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author Thanh Nhat
 */
@Entity
@Table(name = "workout_plan")
@NamedQueries({
    @NamedQuery(name = "WorkoutPlan.findAll", query = "SELECT w FROM WorkoutPlan w"),
    @NamedQuery(name = "WorkoutPlan.findByPlanId", query = "SELECT w FROM WorkoutPlan w WHERE w.planId = :planId"),
    @NamedQuery(name = "WorkoutPlan.findByPlanName", query = "SELECT w FROM WorkoutPlan w WHERE w.planName = :planName"),
    @NamedQuery(name = "WorkoutPlan.findByIsTemplate", query = "SELECT w FROM WorkoutPlan w WHERE w.isTemplate = :isTemplate"),
    @NamedQuery(name = "WorkoutPlan.findByCreatedAt", query = "SELECT w FROM WorkoutPlan w WHERE w.createdAt = :createdAt"),
@NamedQuery(name = "WorkoutPlan.findByUserId",
            query = "SELECT w FROM WorkoutPlan w WHERE w.userId.userId = :userId ORDER BY w.createdAt DESC"),
    @NamedQuery(name = "WorkoutPlan.findTemplates",
            query = "SELECT w FROM WorkoutPlan w WHERE w.isTemplate = TRUE ORDER BY w.createdAt DESC")})
public class WorkoutPlan implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "plan_id")
    private Integer planId;
    @Basic(optional = false)
    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.Size(min = 1, max = 100)
    @Column(name = "plan_name")
    private String planName;
    @Column(name = "is_template")
    private Boolean isTemplate;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @JoinColumn(name = "goal_id", referencedColumnName = "goal_id")
    @ManyToOne
    private Goal goalId;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne
    private User userId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "planId")
    private Set<PlanDetail> planDetailSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "planId")
    private Set<UserWorkoutHistory> userWorkoutHistorySet;

    public WorkoutPlan() {
    }

    public WorkoutPlan(Integer planId) {
        this.planId = planId;
    }

    public WorkoutPlan(Integer planId, String planName) {
        this.planId = planId;
        this.planName = planName;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public Boolean getIsTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(Boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Goal getGoalId() {
        return goalId;
    }

    public void setGoalId(Goal goalId) {
        this.goalId = goalId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public Set<PlanDetail> getPlanDetailSet() {
        return planDetailSet;
    }

    public void setPlanDetailSet(Set<PlanDetail> planDetailSet) {
        this.planDetailSet = planDetailSet;
    }

    public Set<UserWorkoutHistory> getUserWorkoutHistorySet() {
        return userWorkoutHistorySet;
    }

    public void setUserWorkoutHistorySet(Set<UserWorkoutHistory> userWorkoutHistorySet) {
        this.userWorkoutHistorySet = userWorkoutHistorySet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (planId != null ? planId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof WorkoutPlan)) {
            return false;
        }
        WorkoutPlan other = (WorkoutPlan) object;
        if ((this.planId == null && other.planId != null) || (this.planId != null && !this.planId.equals(other.planId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ntn.pojo.WorkoutPlan[ planId=" + planId + " ]";
    }
    
}
