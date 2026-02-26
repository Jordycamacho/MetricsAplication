package com.fitapp.backend.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final CacheManager cacheManager;

    public void clearSportCache(Long sportId, String userEmail) {
        log.debug("CACHE_EVICT | sport-by-id | sportId={}", sportId);
        Objects.requireNonNull(cacheManager.getCache("sport-by-id")).evict(sportId);
        Objects.requireNonNull(cacheManager.getCache("user-sports")).evict(userEmail);
    }

    public void clearRoutineCache(Long routineId, String userEmail) {
        log.debug("CACHE_EVICT | routines | routineId={}", routineId);
        Objects.requireNonNull(cacheManager.getCache("routines")).evict(routineId + "_" + userEmail);
        Objects.requireNonNull(cacheManager.getCache("userRoutines")).evict(userEmail);
    }

    public void clearAllCaches() {
        log.warn("CACHE_EVICT_ALL");
        cacheManager.getCacheNames()
            .forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }
}