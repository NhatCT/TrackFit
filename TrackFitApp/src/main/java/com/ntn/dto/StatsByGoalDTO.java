package com.ntn.dto;

public class StatsByGoalDTO {

    private String goalType;
    private long completedCount;

    public StatsByGoalDTO() {
    }

    public StatsByGoalDTO(String goalType, long completedCount) {
        this.goalType = goalType;
        this.completedCount = completedCount;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }
}
