package com.fitapp.backend.infrastructure.persistence.adapter.out;

import com.fitapp.backend.application.ports.output.TokenBlacklistPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Blacklist de tokens en Redis con TTL = tiempo restante del JWT.
 * Si Redis no está disponible, falla silenciosamente para no bloquear la app.
 *  - blacklist()      → loguea warning, no lanza excepción
 *  - isBlacklisted()  → retorna false (fail-open: el token se considera válido)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisTokenBlacklistAdapter implements TokenBlacklistPort {

    private static final String PREFIX = "blacklist:token:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void blacklist(String token, long ttlSeconds) {
        if (ttlSeconds <= 0) return;
        try {
            redisTemplate.opsForValue()
                    .set(PREFIX + token, "1", Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("Redis no disponible - token no añadido a blacklist: {}", e.getMessage());
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
        } catch (Exception e) {
            log.warn("Redis no disponible - asumiendo token válido: {}", e.getMessage());
            return false; // fail-open: si Redis cae, no bloqueamos al usuario
        }
    }
}