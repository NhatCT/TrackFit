package com.ntn.dto;

import java.util.Date;

public class StatsDailyPointDTO {

    private Date date;       // ngày (00:00)
    private long sessions;   // tổng buổi
    private long completed;  // buổi COMPLETED
    private long minutes;    // tổng phút ước tính

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getSessions() {
        return sessions;
    }

    public void setSessions(long sessions) {
        this.sessions = sessions;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getMinutes() {
        return minutes;
    }

    public void setMinutes(long minutes) {
        this.minutes = minutes;
    }
}
