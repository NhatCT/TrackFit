// com/ntn/dto/AdminStatsSummaryDTO.java
package com.ntn.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AdminStatsSummaryDTO implements Serializable {

    // Khoảng thời gian báo cáo (để hiển thị lại trên form lọc)
    private Date from;
    private Date to;

    // KPI tổng quan
    private long totalUsers;
    private long totalExercises;
    private long totalPlans;
    private long totalTemplatePlans;

    // KPI hoạt động
    private long totalSessions;    // tổng record lịch sử trong khoảng thời gian
    private long totalCompleted;   // số buổi COMPLETED
    private long totalMinutes;     // tổng phút ước tính

    // Phát sinh thêm: tiện hiển thị nhanh trên dashboard
    public double getAvgMinutesPerCompleted() {
        return totalCompleted == 0 ? 0.0 : (double) totalMinutes / totalCompleted;
    }

    private List<StatsDailyPointDTO> daily = Collections.emptyList();
    private List<StatsByGoalDTO> byGoal = Collections.emptyList();

    // Getters/Setters
    public Date getFrom() { return from; }
    public void setFrom(Date from) { this.from = from; }
    public Date getTo() { return to; }
    public void setTo(Date to) { this.to = to; }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getTotalExercises() { return totalExercises; }
    public void setTotalExercises(long totalExercises) { this.totalExercises = totalExercises; }
    public long getTotalPlans() { return totalPlans; }
    public void setTotalPlans(long totalPlans) { this.totalPlans = totalPlans; }
    public long getTotalTemplatePlans() { return totalTemplatePlans; }
    public void setTotalTemplatePlans(long totalTemplatePlans) { this.totalTemplatePlans = totalTemplatePlans; }
    public long getTotalSessions() { return totalSessions; }
    public void setTotalSessions(long totalSessions) { this.totalSessions = totalSessions; }
    public long getTotalCompleted() { return totalCompleted; }
    public void setTotalCompleted(long totalCompleted) { this.totalCompleted = totalCompleted; }
    public long getTotalMinutes() { return totalMinutes; }
    public void setTotalMinutes(long totalMinutes) { this.totalMinutes = totalMinutes; }

    public List<StatsDailyPointDTO> getDaily() { return daily; }
    public void setDaily(List<StatsDailyPointDTO> daily) { this.daily = daily != null ? daily : Collections.emptyList(); }
    public List<StatsByGoalDTO> getByGoal() { return byGoal; }
    public void setByGoal(List<StatsByGoalDTO> byGoal) { this.byGoal = byGoal != null ? byGoal : Collections.emptyList(); }
}
