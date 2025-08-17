package com.ntn.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class WorkoutPlanCreateRequest {

    @NotBlank(message = "{plan.planName.notBlank}")
    @Size(max = 100, message = "{plan.planName.size}")
    private String planName;

    private Integer goalId;
    private Boolean isTemplate;

    @Valid
    @Size(max = 100, message = "{plan.details.size}")
    private List<PlanDetailItemDTO> details;

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public Integer getGoalId() {
        return goalId;
    }

    public void setGoalId(Integer goalId) {
        this.goalId = goalId;
    }

    public Boolean getIsTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(Boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    public List<PlanDetailItemDTO> getDetails() {
        return details;
    }

    public void setDetails(List<PlanDetailItemDTO> details) {
        this.details = details;
    }
}
