package com.fitapp.backend.notification.infrastructure.ratelimit;

import com.fitapp.backend.notification.domain.exception.EmailRateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailRateLimitService {

    private static final String PREFIX = "email:rate:";
    private static final int MAX_REQUESTS = 3;
    private static final Duration WINDOW = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;

    public void checkAndIncrement(String action, String identifier) {
        String key = PREFIX + action + ":" + identifier.toLowerCase();
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, WINDOW);
            }
            if (count != null && count > MAX_REQUESTS) {
                throw new EmailRateLimitExceededException();
            }
        } catch (EmailRateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis no disponible para rate limit de email: {}", e.getMessage());
        }
    }
}
