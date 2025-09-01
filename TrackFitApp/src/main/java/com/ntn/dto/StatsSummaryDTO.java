package com.ntn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class StatsSummaryDTO {

    private long totalSessions;
    private long totalCompleted;
    private long totalMinutes;
    private List<StatsDailyPointDTO> daily;
    private List<StatsByGoalDTO> byGoal;

    private String topExerciseName;
    private List<StatsByExerciseDTO> byExercise;

    // === Alias cho FE cũ ===
    @JsonProperty("sessions")
    public long getSessionsAlias() {
        return totalCompleted;
    }

    @JsonProperty("dailyMinutes")
    public List<StatsDailyMinutesDTO> getDailyMinutesAlias() {
        if (daily == null) {
            return List.of();
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        return daily.stream()
                .map(p -> new StatsDailyMinutesDTO(fmt.format(p.getDate().toInstant()), p.getMinutes()))
                .collect(Collectors.toList());
    }

    @JsonProperty("exerciseSessions")
    public List<ExerciseSessionsDTO> getExerciseSessionsAlias() {
        if (byExercise == null) {
            return List.of();
        }
        return byExercise.stream()
                .map(x -> new ExerciseSessionsDTO(x.getName(), x.getCompletedCount()))
                .collect(Collectors.toList());
    }

    // === Getters/Setters gốc ===
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

    public String getTopExerciseName() {
        return topExerciseName;
    }

    public void setTopExerciseName(String topExerciseName) {
        this.topExerciseName = topExerciseName;
    }

    public List<StatsByExerciseDTO> getByExercise() {
        return byExercise;
    }

    public void setByExercise(List<StatsByExerciseDTO> byExercise) {
        this.byExercise = byExercise;
    }
}
