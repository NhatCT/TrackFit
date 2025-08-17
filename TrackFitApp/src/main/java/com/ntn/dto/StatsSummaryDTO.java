package com.ntn.dto;

import java.util.List;

public class StatsSummaryDTO {

    private long totalSessions;
    private long totalCompleted;
    private long totalMinutes;
    private List<StatsDailyPointDTO> daily;
    private List<StatsByGoalDTO> byGoal;

    public long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(long totalSessions) {
        this.totalSessions = totalSessions;
    }

    public long getTotalCompleted() {
        return totalCompleted;
    }

    public void setTotalCompleted(long totalCompleted) {
        this.totalCompleted = totalCompleted;
    }

    public long getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(long totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public List<StatsDailyPointDTO> getDaily() {
        return daily;
    }

    public void setDaily(List<StatsDailyPointDTO> daily) {
        this.daily = daily;
    }

    public List<StatsByGoalDTO> getByGoal() {
        return byGoal;
    }

    public void setByGoal(List<StatsByGoalDTO> byGoal) {
        this.byGoal = byGoal;
    }
}
