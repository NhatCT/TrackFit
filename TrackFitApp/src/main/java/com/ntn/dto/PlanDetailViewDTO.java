package com.ntn.dto;

public class PlanDetailViewDTO {

    private Integer detailId;
    private Integer exerciseId;
    private String exerciseName;
    private Integer dayOfWeek;
    private Integer duration;

    // getters/setters ...
    /**
     * @return the detailId
     */
    public Integer getDetailId() {
        return detailId;
    }

    /**
     * @param detailId the detailId to set
     */
    public void setDetailId(Integer detailId) {
        this.detailId = detailId;
    }

    /**
     * @return the exerciseId
     */
    public Integer getExerciseId() {
        return exerciseId;
    }

    /**
     * @param exerciseId the exerciseId to set
     */
    public void setExerciseId(Integer exerciseId) {
        this.exerciseId = exerciseId;
    }

    /**
     * @return the exerciseName
     */
    public String getExerciseName() {
        return exerciseName;
    }

    /**
     * @param exerciseName the exerciseName to set
     */
    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    /**
     * @return the dayOfWeek
     */
    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * @param dayOfWeek the dayOfWeek to set
     */
    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    /**
     * @return the duration
     */
    public Integer getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
