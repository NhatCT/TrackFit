package com.ntn.services;

public interface ChatQuotaService {

    int getDailyLimit();

    /** Trả về số tin còn lại trong ngày (Integer.MAX_VALUE nếu PRO). */
    int getRemaining(Integer userId, boolean isPremium);

    /**
     * Kiểm tra và tăng bộ đếm. Ném ChatQuotaExceededException nếu vượt giới hạn Free.
     * PRO user luôn pass.
     */
    void consumeOrThrow(Integer userId, boolean isPremium);
}
