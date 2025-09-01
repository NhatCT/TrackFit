package com.ntn.dto;

public class ExerciseSessionsDTO {

    private String name;
    private long sessions;

    public ExerciseSessionsDTO() {
    }

    public ExerciseSessionsDTO(String name, long sessions) {
        this.name = name;
        this.sessions = sessions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSessions() {
        return sessions;
    }

    public void setSessions(long sessions) {
        this.sessions = sessions;
    }
}
