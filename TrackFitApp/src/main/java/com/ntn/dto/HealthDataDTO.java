package com.ntn.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public class HealthDataDTO {

    @NotNull(message = "{healthData.height.notNull}")
    @Positive(message = "{healthData.height.positive}")
    private Double height;

    @NotNull(message = "{healthData.weight.notNull}")
    @Positive(message = "{healthData.weight.positive}")
    private Double weight;

    @NotBlank(message = "{healthData.gender.notBlank}")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "{healthData.gender.pattern}")
    private String gender;

    @NotBlank(message = "{healthData.goalType.notBlank}")
    @Size(max = 50, message = "{healthData.goalType.size}")
    private String goalType;

    @NotNull(message = "{healthData.workoutDays.notNull}")
    @Min(value = 1, message = "{healthData.workoutDays.min}")
    @Max(value = 7, message = "{healthData.workoutDays.max}")
    private Integer workoutDays;

    @NotNull(message = "{healthData.birthDate.notNull}")
    @Past(message = "{healthData.birthDate.past}")
    private LocalDate birthDate;

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public Integer getWorkoutDays() {
        return workoutDays;
    }

    public void setWorkoutDays(Integer workoutDays) {
        this.workoutDays = workoutDays;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
