package com.ntn.dto;

import java.util.List;
import java.util.Map;

public class AiRankRequest {

    public static class UserInfo {

        public Integer id;
        public String goalType;
        public String gender;
    }

    public static class Candidate {

        public Integer exerciseId;
        public String name;
        public String muscleGroup;
        public Integer minutes;     // có metadata thì điền
        public String difficulty;   // có metadata thì điền
    }

    private UserInfo user;
    private Map<String, Object> context;
    private List<Candidate> candidates;

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }
}
