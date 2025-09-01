package com.ntn.dto;

import java.util.Date;

public class RecommendationItemDTO {

    private Integer exercisesId;      // id bài tập gốc (nếu có)
    private String name;
    private String description;       // fallback từ reason nếu trống
    private String muscleGroup;
    private String targetGoal;        // ví dụ: "15’ • AI 0.92"
    private String videoUrl;
    private Date createdAt;

    // Thông tin AI (để FE có thể dùng nếu muốn)
    private Integer estimatedMinutes;
    private Double score;
    private String reason;

    // getters/setters
    public Integer getExercisesId() {
        return exercisesId;
    }

    public void setExercisesId(Integer exercisesId) {
        this.exercisesId = exercisesId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public String getTargetGoal() {
        return targetGoal;
    }

    public void setTargetGoal(String targetGoal) {
        this.targetGoal = targetGoal;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
