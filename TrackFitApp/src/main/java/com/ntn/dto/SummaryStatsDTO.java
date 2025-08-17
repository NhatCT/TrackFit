package com.ntn.dto;

public class SummaryStatsDTO {
    private long sessions;          
    private long totalMinutes;      
    private Double avgMinutes;    

    public SummaryStatsDTO(long sessions, long totalMinutes) {
        this.sessions = sessions;
        this.totalMinutes = totalMinutes;
        this.avgMinutes = sessions > 0 ? (totalMinutes * 1.0) / sessions : 0.0;
    }
    public long getSessions() { return sessions; }
    public long getTotalMinutes() { return totalMinutes; }
    public Double getAvgMinutes() { return avgMinutes; }
}
