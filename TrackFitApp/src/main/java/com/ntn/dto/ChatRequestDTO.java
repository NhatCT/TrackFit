package com.ntn.dto;

public class ChatRequestDTO {

    private String sessionId;
    private String question;
    private Integer topK = 4;

    public ChatRequestDTO() {
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String s) {
        this.sessionId = s;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String q) {
        this.question = q;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer k) {
        this.topK = k;
    }
}
