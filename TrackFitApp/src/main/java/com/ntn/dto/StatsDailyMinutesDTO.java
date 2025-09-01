package com.ntn.dto;

public class StatsDailyMinutesDTO {

    private String date;   // yyyy-MM-dd
    private long minutes;

    public StatsDailyMinutesDTO() {
    }

    public StatsDailyMinutesDTO(String date, long minutes) {
        this.date = date;
        this.minutes = minutes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getMinutes() {
        return minutes;
    }

    public void setMinutes(long minutes) {
        this.minutes = minutes;
    }
}
