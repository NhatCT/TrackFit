package com.ntn.dto;

import java.io.Serializable;
import java.util.Date;


public class WorkoutPlanListItemDTO implements Serializable {
    private Integer planId;
    private String planName;
    private Boolean isTemplate;
    private Date createdAt;

    public WorkoutPlanListItemDTO() {
    }
    public WorkoutPlanListItemDTO(Integer planId, String planName, Boolean isTemplate, Date createdAt) {
        this.planId = planId;
        this.planName = planName;
        this.isTemplate = isTemplate;
        this.createdAt = createdAt;
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
}
