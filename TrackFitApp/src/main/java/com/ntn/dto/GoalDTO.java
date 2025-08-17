package com.ntn.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class GoalDTO {

    @NotBlank(message = "{goal.goalType.notBlank}")
    @Size(max = 50, message = "{goal.goalType.size}")
    private String goalType;

    @NotNull(message = "{goal.workoutDuration.notNull}")
    @Min(value = 1, message = "{goal.workoutDuration.min}")
    @Max(value = 365, message = "{goal.workoutDuration.max}")
    private Integer workoutDuration;

    @NotBlank(message = "{goal.intensity.notBlank}")
    @Pattern(regexp = "^(Low|Medium|High)$", message = "{goal.intensity.pattern}")
    private String intensity;

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
}
