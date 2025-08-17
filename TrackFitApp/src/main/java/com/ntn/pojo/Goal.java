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
@Table(name = "goal")
@NamedQueries({
    @NamedQuery(name = "Goal.findAll", query = "SELECT g FROM Goal g"),
    @NamedQuery(name = "Goal.findByGoalId", query = "SELECT g FROM Goal g WHERE g.goalId = :goalId"),
    @NamedQuery(name = "Goal.findByGoalType", query = "SELECT g FROM Goal g WHERE g.goalType = :goalType"),
    @NamedQuery(name = "Goal.findByWorkoutDuration", query = "SELECT g FROM Goal g WHERE g.workoutDuration = :workoutDuration"),
    @NamedQuery(name = "Goal.findByIntensity", query = "SELECT g FROM Goal g WHERE g.intensity = :intensity"),
    @NamedQuery(name = "Goal.findByCreatedAt", query = "SELECT g FROM Goal g WHERE g.createdAt = :createdAt"),
    @NamedQuery(name = "Goal.findByUserId", query = "SELECT g FROM Goal g WHERE g.userId.userId = :userId")})
public class Goal implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "goal_id")
    private Integer goalId;
    @jakarta.validation.constraints.Size(max = 50)
    @Column(name = "goal_type")
    private String goalType;
    @Column(name = "workout_duration")
    private Integer workoutDuration;
    @jakarta.validation.constraints.Size(max = 20)
    @Column(name = "intensity")
    private String intensity;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @OneToMany(mappedBy = "goalId")
    private Set<WorkoutPlan> workoutPlanSet;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false)
    private User userId;

    public Goal() {
    }

    public Goal(Integer goalId) {
        this.goalId = goalId;
    }

    public Integer getGoalId() {
        return goalId;
    }

    public void setGoalId(Integer goalId) {
        this.goalId = goalId;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public Integer getWorkoutDuration() {
        return workoutDuration;
    }

    public void setWorkoutDuration(Integer workoutDuration) {
        this.workoutDuration = workoutDuration;
    }

    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Set<WorkoutPlan> getWorkoutPlanSet() {
        return workoutPlanSet;
    }

    public void setWorkoutPlanSet(Set<WorkoutPlan> workoutPlanSet) {
        this.workoutPlanSet = workoutPlanSet;
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
        hash += (goalId != null ? goalId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Goal)) {
            return false;
        }
        Goal other = (Goal) object;
        if ((this.goalId == null && other.goalId != null) || (this.goalId != null && !this.goalId.equals(other.goalId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ntn.pojo.Goal[ goalId=" + goalId + " ]";
    }

}
