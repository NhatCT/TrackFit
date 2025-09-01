package com.ntn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class StatsDailyPointDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Ho_Chi_Minh")
    private Date date;       // ngày (00:00 VN)
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
