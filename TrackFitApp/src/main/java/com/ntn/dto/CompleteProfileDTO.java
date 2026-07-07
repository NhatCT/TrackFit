package com.ntn.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class CompleteProfileDTO {
    @NotNull(message = "Chiều cao không được để trống")
    @Min(value = 50, message = "Chiều cao tối thiểu là 50cm")
    @Max(value = 250, message = "Chiều cao tối đa là 250cm")
    private Double height;

    @NotNull(message = "Cân nặng không được để trống")
    @Min(value = 20, message = "Cân nặng tối thiểu là 20kg")
    @Max(value = 300, message = "Cân nặng tối đa là 300kg")
    private Double weight;

    @NotBlank(message = "Giới tính không được để trống")
    @Pattern(regexp = "^(Male|Female)$", message = "Giới tính phải là Male hoặc Female")
    private String gender;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải ở quá khứ")
    private LocalDate birthDate;

    @NotBlank(message = "Mục tiêu không được để trống")
    private String goalType;

    @NotBlank(message = "Cường độ không được để trống")
    private String intensity;

    // Getters and Setters
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }
}
