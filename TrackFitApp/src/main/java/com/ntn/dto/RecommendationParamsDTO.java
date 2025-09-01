package com.ntn.dto;

public class RecommendationParamsDTO {

    private Integer size;
    private String kw;
    private Integer availableMinutes;
    private String intensity;
    private String goalType;

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
