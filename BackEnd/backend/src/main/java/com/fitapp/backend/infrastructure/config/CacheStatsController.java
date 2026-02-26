package com.fitapp.backend.infrastructure.config;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal/cache")
@RequiredArgsConstructor
public class CacheStatsController {

    private final CacheManager cacheManager; 

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> result = new LinkedHashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
            if (cache == null) return;

            CacheStats stats = cache.getNativeCache().stats();
            Map<String, Object> cacheData = new LinkedHashMap<>();
            cacheData.put("hitRate", String.format("%.2f%%", stats.hitRate() * 100));
            cacheData.put("missRate", String.format("%.2f%%", stats.missRate() * 100));
            cacheData.put("hitCount", stats.hitCount());
            cacheData.put("missCount", stats.missCount());
            cacheData.put("evictionCount", stats.evictionCount());
            cacheData.put("size", cache.getNativeCache().estimatedSize());

            result.put(cacheName, cacheData);
        });

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Void> clearCache(@PathVariable String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache == null) return ResponseEntity.notFound().build();
        cache.clear();
        return ResponseEntity.noContent().build();
    }
}