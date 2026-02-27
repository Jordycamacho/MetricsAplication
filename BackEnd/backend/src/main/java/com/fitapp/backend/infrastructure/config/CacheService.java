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

    // ── Sports ────────────────────────────────────────────────────────────────

    public void clearSportCache(Long sportId, String userEmail) {
        log.debug("CACHE_EVICT | sport-by-id | sportId={}", sportId);
        evict("sport-by-id", sportId);
        evict("user-sports", userEmail);
    }

    // ── Routines ──────────────────────────────────────────────────────────────

    public void clearRoutineCache(Long routineId, String userEmail) {
        log.debug("CACHE_EVICT | routines | routineId={}", routineId);
        evict("routines", routineId + "_" + userEmail);
        evict("userRoutines", userEmail);
    }

    // ── Categories ────────────────────────────────────────────────────────────

    /**
     * Llamar después de crear, actualizar o eliminar una categoría personal.
     * No toca predefined-categories porque las personales no las afectan.
     */
    public void clearUserCategoryCache(Long categoryId, String userEmail) {
        log.debug("CACHE_EVICT | categories | categoryId={} | user={}", categoryId, userEmail);
        evict("category-by-id", categoryId);
        evict("user-categories", userEmail);
        // available-categories incluye las del usuario → también hay que limpiar
        evictByPrefix("available-categories", userEmail);
    }

    /**
     * Llamar cuando cambia una categoría predefinida (operación de admin).
     * Limpia todas las caches relacionadas porque las predefinidas aparecen en todos los listados.
     */
    public void clearPredefinedCategoryCache(Long categoryId) {
        log.warn("CACHE_EVICT | predefined-categories | categoryId={}", categoryId);
        evict("category-by-id", categoryId);
        clearAll("predefined-categories");
        clearAll("available-categories");
        clearAll("most-used-categories");
    }

    /**
     * Limpia la entrada de available-categories para un usuario+sport concreto.
     * Clave usada: userId_sportId (ver ExerciseCategoryServiceImpl).
     */
    public void clearAvailableCategoryCache(Long userId, Long sportId) {
        String key = userId + "_" + (sportId != null ? sportId : "null");
        log.debug("CACHE_EVICT | available-categories | key={}", key);
        evict("available-categories", key);
    }

    // ── Global ────────────────────────────────────────────────────────────────

    public void clearAllCaches() {
        log.warn("CACHE_EVICT_ALL");
        cacheManager.getCacheNames()
                .forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void evict(String cacheName, Object key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        } else {
            log.warn("CACHE_EVICT_SKIP | cache not found | name={}", cacheName);
        }
    }

    private void evictByPrefix(String cacheName, String prefix) {
        log.debug("CACHE_EVICT_BY_PREFIX | cache={} | prefix={}", cacheName, prefix);
        clearAll(cacheName);
    }

    private void clearAll(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}