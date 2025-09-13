package com.ntn.dto;

public class RankCandidateDTO {

    private Object id;
    private String title;
    private String text;
    private String group;

    public RankCandidateDTO() {
    }

    public RankCandidateDTO(Object id, String title, String text, String group) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.group = group;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
