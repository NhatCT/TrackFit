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
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
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
@Table(name = "exercises", indexes = {
    @Index(name = "idx_exercises_name", columnList = "name")
})
@NamedQueries({
    @NamedQuery(name = "Exercises.findAll", query = "SELECT e FROM Exercises e"),
    @NamedQuery(name = "Exercises.findByExercisesId", query = "SELECT e FROM Exercises e WHERE e.exercisesId = :exercisesId"),
    @NamedQuery(name = "Exercises.findByName", query = "SELECT e FROM Exercises e WHERE e.name = :name"),
    @NamedQuery(name = "Exercises.findByTargetGoal", query = "SELECT e FROM Exercises e WHERE e.targetGoal = :targetGoal"),
    @NamedQuery(name = "Exercises.findByMuscleGroup", query = "SELECT e FROM Exercises e WHERE e.muscleGroup = :muscleGroup"),
    @NamedQuery(name = "Exercises.findByVideoUrl", query = "SELECT e FROM Exercises e WHERE e.videoUrl = :videoUrl"),
    @NamedQuery(name = "Exercises.findByCreatedAt", query = "SELECT e FROM Exercises e WHERE e.createdAt = :createdAt"),
    @NamedQuery(name = "Exercises.findByNameLike", query = "SELECT e FROM Exercises e WHERE LOWER(e.name) LIKE LOWER(:kw)")}
)
public class Exercises implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "exercises_id")
    private Integer exercisesId;
    @Basic(optional = false)
    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.Size(min = 1, max = 100)
    @Column(name = "name")
    private String name;
    @jakarta.validation.constraints.Size(max = 50)
    @Column(name = "target_goal")
    private String targetGoal;
    @jakarta.validation.constraints.Size(max = 50)
    @Column(name = "muscle_group")
    private String muscleGroup;
    @jakarta.validation.constraints.Size(max = 255)
    @Column(name = "video_url")
    private String videoUrl;
    @Lob
    @jakarta.validation.constraints.Size(max = 65535)
    @Column(name = "description")
    private String description;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "exercisesId")
    private Set<PlanDetail> planDetailSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "exercisesId")
    private Set<UserWorkoutHistory> userWorkoutHistorySet;

    public Exercises() {
    }

    public Exercises(Integer exercisesId) {
        this.exercisesId = exercisesId;
    }

    public Exercises(Integer exercisesId, String name) {
        this.exercisesId = exercisesId;
        this.name = name;
    }

    public Integer getExercisesId() {
        return exercisesId;
    }

    public void setExercisesId(Integer exercisesId) {
        this.exercisesId = exercisesId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetGoal() {
        return targetGoal;
    }

    public void setTargetGoal(String targetGoal) {
        this.targetGoal = targetGoal;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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
        // Stable, non-zero hash for transient (null-id) instances so HashSet does not drop distinct new entities.
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Exercises)) {
            return false;
        }
        Exercises other = (Exercises) object;
        // Two transient instances (null id) are only equal by reference, handled above.
        if (this.exercisesId == null || other.exercisesId == null) {
            return false;
        }
        return this.exercisesId.equals(other.exercisesId);
    }

    @Override
    public String toString() {
        return "com.ntn.pojo.Exercises[ exercisesId=" + exercisesId + " ]";
    }

}
