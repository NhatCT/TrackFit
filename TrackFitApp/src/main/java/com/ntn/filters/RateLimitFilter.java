package com.ntn.filters;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

public class RateLimitFilter implements Filter {

    private final StringRedisTemplate redisTemplate;
    private static final int LIMIT = 60; // Max 60 requests per minute
    private static final String KEY_PREFIX = "rate:ip:";

    public RateLimitFilter(RedisConnectionFactory connectionFactory) {
        this.redisTemplate = new StringRedisTemplate(connectionFactory);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Init logic if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(httpRequest);
        String key = KEY_PREFIX + ip;

        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null) {
                if (count == 1) {
                    redisTemplate.expire(key, Duration.ofSeconds(60));
                } else {
                    // Fallback to restore TTL in case of failure/interruption
                    Long ttl = redisTemplate.getExpire(key);
                    if (ttl != null && ttl == -1) {
                        redisTemplate.expire(key, Duration.ofSeconds(60));
                    }
                }

                if (count > LIMIT) {
                    httpResponse.setStatus(429); // Too Many Requests
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.getWriter().write("{\"message\": \"Spam quá nhanh! Vui lòng thử lại sau.\"}");
                    return;
                }
            }
        } catch (Exception e) {
            // Log Redis error and degrade gracefully (allow traffic if Redis is down)
            System.err.println("Redis rate limit error: " + e.getMessage());
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Destroy logic if needed
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
