package com.ntn.dto;

public class RecommendationParamsDTO {

    private Integer size;              // số item trả về, mặc định 10
    private String kw;                 // tìm theo tên bài tập (optional)
    private Integer availableMinutes;  // thời lượng rảnh, mặc định 25
    private String intensity;          // Low/Medium/High (optional)
    private String goalType;           // lose_weight/gain_muscle... (optional)

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getKw() {
        return kw;
    }

    public void setKw(String kw) {
        this.kw = kw;
    }

    public Integer getAvailableMinutes() {
        return availableMinutes;
    }

    public void setAvailableMinutes(Integer availableMinutes) {
        this.availableMinutes = availableMinutes;
    }

    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }
}
