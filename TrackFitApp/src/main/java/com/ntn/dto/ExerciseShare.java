// com/ntn/dto/ExerciseShare.java
package com.ntn.dto;

public class ExerciseShare {

    private final Integer exerciseId;
    private final String name;
    private final long sessions;
    private final long minutes;

    public ExerciseShare(Integer exerciseId, String name, long sessions, long minutes) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.sessions = sessions;
        this.minutes = minutes;
    }

    public Integer getExerciseId() {
        return exerciseId;
    }

    public String getName() {
        return name;
    }

    public long getSessions() {
        return sessions;
    }

    public long getMinutes() {
        return minutes;
    }
}
