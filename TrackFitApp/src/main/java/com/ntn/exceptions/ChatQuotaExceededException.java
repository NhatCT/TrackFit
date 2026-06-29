package com.ntn.exceptions;

public class ChatQuotaExceededException extends RuntimeException {
    private final int dailyLimit;

    public ChatQuotaExceededException(int dailyLimit) {
        super("Bạn đã dùng hết " + dailyLimit + " tin nhắn miễn phí hôm nay. Nâng cấp PRO để chat không giới hạn.");
        this.dailyLimit = dailyLimit;
    }

    public int getDailyLimit() {
        return dailyLimit;
    }
}
