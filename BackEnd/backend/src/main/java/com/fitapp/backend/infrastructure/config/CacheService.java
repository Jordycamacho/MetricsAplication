package com.fitapp.backend.infrastructure.config;

import org.springframework.cache.CacheManager;

import org.springframework.stereotype.Component;

@Component
public class CacheService {
    private final CacheManager cacheManager = null;

    public void clearRoutineCache(Long routineId, String userEmail) {
        cacheManager.getCache("routines").evict(routineId + "_" + userEmail);
        cacheManager.getCache("userRoutines").evict(userEmail);
    }

    public void clearAllRoutineCaches() {
        cacheManager.getCache("routines").clear();
        cacheManager.getCache("userRoutines").clear();
    }
}
