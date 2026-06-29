package com.ntn.services.impl;

import com.ntn.exceptions.ChatQuotaExceededException;
import com.ntn.services.ChatQuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class ChatQuotaServiceImpl implements ChatQuotaService {

    private static final String KEY_PREFIX = "chat:quota:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${chat.free.daily-limit:3}")
    private int dailyLimit;

    @Override
    public int getDailyLimit() {
        return dailyLimit;
    }

    @Override
    public int getRemaining(Integer userId, boolean isPremium) {
        if (isPremium || userId == null) {
            return Integer.MAX_VALUE;
        }
        long used = currentCount(userId);
        return (int) Math.max(0, dailyLimit - used);
    }

    @Override
    public void consumeOrThrow(Integer userId, boolean isPremium) {
        if (isPremium) {
            return;
        }
        if (userId == null) {
            throw new ChatQuotaExceededException(dailyLimit);
        }
        long used = increment(userId);
        if (used > dailyLimit) {
            throw new ChatQuotaExceededException(dailyLimit);
        }
    }

    private long currentCount(Integer userId) {
        try {
            String v = redisTemplate.opsForValue().get(key(userId));
            return v == null ? 0L : Long.parseLong(v);
        } catch (Exception e) {
            System.err.println("[ChatQuota] Redis read error: " + e.getMessage());
            return 0L;
        }
    }

    private long increment(Integer userId) {
        try {
            String key = key(userId);
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, ttlUntilEndOfDay());
            }
            return count == null ? 1L : count;
        } catch (Exception e) {
            System.err.println("[ChatQuota] Redis increment error: " + e.getMessage());
            return dailyLimit + 1L;
        }
    }

    private String key(Integer userId) {
        return KEY_PREFIX + userId + ":" + LocalDate.now();
    }

    private Duration ttlUntilEndOfDay() {
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        long seconds = Duration.between(LocalDateTime.now(), end).getSeconds();
        return Duration.ofSeconds(Math.max(60, seconds));
    }
}
