package com.ntn.services.impl;

import com.ntn.exceptions.ChatQuotaExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatQuotaServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private ChatQuotaServiceImpl chatQuotaService;

    @BeforeEach
    void setUp() throws Exception {
        Field field = ChatQuotaServiceImpl.class.getDeclaredField("dailyLimit");
        field.setAccessible(true);
        field.setInt(chatQuotaService, 3);
    }

    @Test
    void getDailyLimit_returnsConfiguredLimit() {
        assertEquals(3, chatQuotaService.getDailyLimit());
    }

    @Test
    void getRemaining_premiumUser_returnsMaxValue() {
        int remaining = chatQuotaService.getRemaining(1, true);
        assertEquals(Integer.MAX_VALUE, remaining);
    }

    @Test
    void getRemaining_nullUserId_returnsMaxValue() {
        int remaining = chatQuotaService.getRemaining(null, false);
        assertEquals(Integer.MAX_VALUE, remaining);
    }

    @Test
    void getRemaining_freeUser_zeroUsed_returnsFullLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);

        int remaining = chatQuotaService.getRemaining(1, false);
        assertEquals(3, remaining);
    }

    @Test
    void getRemaining_freeUser_twoUsed_returnsOne() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn("2");

        int remaining = chatQuotaService.getRemaining(1, false);
        assertEquals(1, remaining);
    }

    @Test
    void getRemaining_freeUser_allUsed_returnsZero() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn("3");

        int remaining = chatQuotaService.getRemaining(1, false);
        assertEquals(0, remaining);
    }

    @Test
    void getRemaining_freeUser_overLimit_returnsZero() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn("10");

        int remaining = chatQuotaService.getRemaining(1, false);
        assertEquals(0, remaining);
    }

    @Test
    void consumeOrThrow_premiumUser_noException() {
        assertDoesNotThrow(() -> chatQuotaService.consumeOrThrow(1, true));
    }

    @Test
    void consumeOrThrow_nullUserId_throwsException() {
        assertThrows(ChatQuotaExceededException.class,
                () -> chatQuotaService.consumeOrThrow(null, false));
    }

    @Test
    void consumeOrThrow_withinLimit_noException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(1L);

        assertDoesNotThrow(() -> chatQuotaService.consumeOrThrow(1, false));
    }

    @Test
    void consumeOrThrow_exceedsLimit_throwsException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(4L);

        assertThrows(ChatQuotaExceededException.class,
                () -> chatQuotaService.consumeOrThrow(1, false));
    }

    @Test
    void getRemaining_redisError_returnsFullLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenThrow(new RuntimeException("Redis down"));

        int remaining = chatQuotaService.getRemaining(1, false);
        assertEquals(3, remaining);
    }
}
