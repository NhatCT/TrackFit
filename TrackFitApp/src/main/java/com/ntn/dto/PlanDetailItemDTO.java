package com.ntn.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PlanDetailItemDTO {

    @NotNull(message = "{planDetail.exerciseId.notNull}")
    private Integer exerciseId;

    @NotNull(message = "{planDetail.dayOfWeek.notNull}")
    @Min(value = 1, message = "{planDetail.dayOfWeek.min}")
    @Max(value = 7, message = "{planDetail.dayOfWeek.max}")
    private Integer dayOfWeek;

    // phút, có thể null
    @Min(value = 1, message = "{planDetail.duration.min}")
    @Max(value = 600, message = "{planDetail.duration.max}")
    private Integer duration;

    // getters/setters
    public Integer getExerciseId() { return exerciseId; }
    public void setExerciseId(Integer exerciseId) { this.exerciseId = exerciseId; }
    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}
