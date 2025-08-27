package com.ntn.dto;

import java.util.Date;
import java.util.List;

public class WorkoutPlanResponseDTO {

    private Integer planId;
    private String planName;
    private Boolean isTemplate;
    private Date createdAt;
    private Integer goalId;
    private Integer userId;
    private String goalName;  // ➕
    private String userName;  // ➕

    private List<PlanDetailViewDTO> details;

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

    public Integer getGoalId() {
        return goalId;
    }

    public void setGoalId(Integer goalId) {
        this.goalId = goalId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<PlanDetailViewDTO> getDetails() {
        return details;
    }

    public void setDetails(List<PlanDetailViewDTO> details) {
        this.details = details;
    }
}
