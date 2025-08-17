// com.ntn.dto.stats.GoalStatsDTO.java
package com.ntn.dto;

public class GoalStatsDTO {
    private String goalType;   // có thể null nếu kế hoạch không gắn Goal
    private long sessions;
    private long totalMinutes;

    public GoalStatsDTO(String goalType, long sessions, long totalMinutes) {
        this.goalType = goalType;
        this.sessions = sessions;
        this.totalMinutes = totalMinutes;
    }
    public String getGoalType() { return goalType; }
    public long getSessions() { return sessions; }
    public long getTotalMinutes() { return totalMinutes; }
}
