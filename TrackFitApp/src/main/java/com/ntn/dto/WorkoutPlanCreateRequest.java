package com.ntn.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkoutPlanCreateRequest {

    // Admin có thể set để tạo hộ user; user thường để null
    private Integer userId;

    @NotBlank(message = "{plan.planName.notBlank}")
    @Size(max = 100, message = "{plan.planName.size}")
    private String planName;

    private Integer goalId;

    @JsonProperty("isTemplate")
    private Boolean isTemplate; // null => xử lý default ở service

    @Valid
    @Size(max = 100, message = "{plan.details.size}")
    private List<PlanDetailItemDTO> details;

    // getters/setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

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

    @JsonProperty("isTemplate")
    public Boolean getIsTemplate() {
        return isTemplate;
    }

    @JsonProperty("isTemplate")
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
