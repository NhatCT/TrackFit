package com.ntn.dto;

import jakarta.validation.constraints.*;

public class GoalDTO {

    // Dành cho hiển thị
    private Integer goalId;
    private String name;

    // Dành cho tạo/cập nhật
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

    // Getters & Setters
    public Integer getGoalId() {
        return goalId;
    }

    public void setGoalId(Integer goalId) {
        this.goalId = goalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
