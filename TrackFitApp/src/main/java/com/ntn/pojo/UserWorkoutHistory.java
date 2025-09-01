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
import java.util.Date;

/**
 *
 * @author Thanh Nhat
 */
@Entity
@Table(name = "user_workout_history")
@NamedQueries({
    @NamedQuery(name = "UserWorkoutHistory.findAll", query = "SELECT u FROM UserWorkoutHistory u"),
    @NamedQuery(name = "UserWorkoutHistory.findByHistoryId", query = "SELECT u FROM UserWorkoutHistory u WHERE u.historyId = :historyId"),
    @NamedQuery(name = "UserWorkoutHistory.findByCompletedAt", query = "SELECT u FROM UserWorkoutHistory u WHERE u.completedAt = :completedAt"),
    @NamedQuery(name = "UserWorkoutHistory.findByStatus", query = "SELECT u FROM UserWorkoutHistory u WHERE u.status = :status")})
public class UserWorkoutHistory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "history_id")
    private Integer historyId;
    @Column(name = "completed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completedAt;
    @jakarta.validation.constraints.Size(max = 9)
    @Column(name = "status")
    private String status;
    @JoinColumn(name = "exercises_id", referencedColumnName = "exercises_id")
    @ManyToOne(optional = false)
    private Exercises exercisesId;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false)
    private User userId;
    @JoinColumn(name = "plan_id", referencedColumnName = "plan_id")
    @ManyToOne(optional = false)
    private WorkoutPlan planId;
    @Column(name = "duration")
    private Integer duration;

    public UserWorkoutHistory() {
    }

    public UserWorkoutHistory(Integer historyId) {
        this.historyId = historyId;
    }

    public Integer getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Integer historyId) {
        this.historyId = historyId;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Exercises getExercisesId() {
        return exercisesId;
    }

    public void setExercisesId(Exercises exercisesId) {
        this.exercisesId = exercisesId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public WorkoutPlan getPlanId() {
        return planId;
    }

    public void setPlanId(WorkoutPlan planId) {
        this.planId = planId;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (historyId != null ? historyId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserWorkoutHistory)) {
            return false;
        }
        UserWorkoutHistory other = (UserWorkoutHistory) object;
        if ((this.historyId == null && other.historyId != null) || (this.historyId != null && !this.historyId.equals(other.historyId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.ntn.pojo.UserWorkoutHistory[ historyId=" + historyId + " ]";
    }

}
