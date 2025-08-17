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
    private List<PlanDetailViewDTO> details;

    // getters/setters ...

    /**
     * @return the planId
     */
    public Integer getPlanId() {
        return planId;
    }

    /**
     * @param planId the planId to set
     */
    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    /**
     * @return the planName
     */
    public String getPlanName() {
        return planName;
    }

    /**
     * @param planName the planName to set
     */
    public void setPlanName(String planName) {
        this.planName = planName;
    }

    /**
     * @return the isTemplate
     */
    public Boolean getIsTemplate() {
        return isTemplate;
    }

    /**
     * @param isTemplate the isTemplate to set
     */
    public void setIsTemplate(Boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    /**
     * @return the createdAt
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt the createdAt to set
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return the goalId
     */
    public Integer getGoalId() {
        return goalId;
    }

    /**
     * @param goalId the goalId to set
     */
    public void setGoalId(Integer goalId) {
        this.goalId = goalId;
    }

    /**
     * @return the userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * @return the details
     */
    public List<PlanDetailViewDTO> getDetails() {
        return details;
    }

    /**
     * @param details the details to set
     */
    public void setDetails(List<PlanDetailViewDTO> details) {
        this.details = details;
    }
}

