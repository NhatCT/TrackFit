package com.ntn.filters;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RateLimitFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(RateLimitFilter.class.getName());

    private final StringRedisTemplate redisTemplate;
    private final boolean trustProxy;
    private final int generalLimit;
    private final int authLimit;

    private static final String KEY_PREFIX = "rate:ip:";
    private static final String AUTH_KEY_PREFIX = "rate:auth:";

    // Authentication endpoints get a separate, stricter per-endpoint limit.
    private static final Set<String> AUTH_PATHS = Set.of(
            "/api/login", "/api/register", "/api/login/google");

    public RateLimitFilter(RedisConnectionFactory connectionFactory) {
        // Safe defaults: do not trust proxy headers, general 60/min, auth 10/min.
        this(connectionFactory, false, 60, 10);
    }

    public RateLimitFilter(RedisConnectionFactory connectionFactory, boolean trustProxy,
            int generalLimit, int authLimit) {
        this.redisTemplate = new StringRedisTemplate(connectionFactory);
        this.trustProxy = trustProxy;
        this.generalLimit = generalLimit;
        this.authLimit = authLimit;
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
        String path = httpRequest.getRequestURI();
        boolean authEndpoint = AUTH_PATHS.contains(path);

        // Auth endpoints use a stricter, separate counter keyed by path+ip so that
        // brute-force attempts against /api/login etc. are throttled independently.
        String key = authEndpoint ? AUTH_KEY_PREFIX + path + ":" + ip : KEY_PREFIX + ip;
        int limit = authEndpoint ? authLimit : generalLimit;

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

                if (count > limit) {
                    httpResponse.setStatus(429); // Too Many Requests
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.getWriter().write("{\"message\": \"Spam quá nhanh! Vui lòng thử lại sau.\"}");
                    return;
                }
            }
        } catch (Exception e) {
            // Fail-open: keep the app usable if Redis is unavailable (failing closed would
            // lock every user, including admins, out of login during a Redis outage), but log
            // a WARNING so the brute-force protection gap is visible to operators.
            LOGGER.log(Level.WARNING,
                    "Redis rate limit unavailable, allowing request (fail-open): " + e.getMessage(), e);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Destroy logic if needed
    }

    private String getClientIp(HttpServletRequest request) {
        // Only trust the client-supplied X-Forwarded-For when a trusted proxy is known to be
        // in front; otherwise it is attacker-controlled and must be ignored.
        if (trustProxy) {
            String xf = request.getHeader("X-Forwarded-For");
            if (xf != null && !xf.isBlank()) {
                String[] parts = xf.split(",");
                // Take the LAST entry (the hop closest to our trusted proxy). Earlier
                // entries can be spoofed by the client and must not be trusted.
                return parts[parts.length - 1].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
