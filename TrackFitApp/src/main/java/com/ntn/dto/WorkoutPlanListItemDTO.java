package com.ntn.dto;

import java.io.Serializable;
import java.util.Date;

public class WorkoutPlanListItemDTO implements Serializable {

    private Integer planId;
    private String planName;
    private Boolean isTemplate;
    private Date createdAt;

    // ➕ Thân thiện UI
    private Integer userId;
    private String userName; // ghép firstName + lastName (fallback username)
    private Integer goalId;
    private String goalName; // lấy từ Goal.name

    public WorkoutPlanListItemDTO() {
    }

    public WorkoutPlanListItemDTO(Integer planId, String planName, Boolean isTemplate, Date createdAt,
            Integer userId, String userName, Integer goalId, String goalName) {
        this.planId = planId;
        this.planName = planName;
        this.isTemplate = isTemplate;
        this.createdAt = createdAt;
        this.userId = userId;
        this.userName = userName;
        this.goalId = goalId;
        this.goalName = goalName;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getGoalId() {
        return goalId;
    }

    public void setGoalId(Integer goalId) {
        this.goalId = goalId;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }
}
