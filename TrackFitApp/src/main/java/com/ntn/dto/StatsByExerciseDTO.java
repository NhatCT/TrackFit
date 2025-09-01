package com.ntn.dto;

public class StatsByExerciseDTO {

    private String name;
    private long completedCount;

    public StatsByExerciseDTO() {
    }

    public StatsByExerciseDTO(String name, long completedCount) {
        this.name = name;
        this.completedCount = completedCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }
}
