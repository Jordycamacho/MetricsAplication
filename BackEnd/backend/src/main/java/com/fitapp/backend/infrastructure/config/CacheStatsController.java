package com.fitapp.backend.infrastructure.config;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheStatsController {
    
    private final CacheManager cacheManager = null;
    
    @GetMapping("/cache-stats")
    public String getCacheStats() {
        StringBuilder stats = new StringBuilder();
        
        cacheManager.getCacheNames().forEach(cacheName -> {
            com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = 
                (com.github.benmanes.caffeine.cache.Cache<?, ?>) 
                ((org.springframework.cache.caffeine.CaffeineCache) 
                 cacheManager.getCache(cacheName)).getNativeCache();
            
            CacheStats cacheStats = caffeineCache.stats();
            stats.append("Cache: ").append(cacheName).append("\n")
                .append("  Hit Rate: ").append(cacheStats.hitRate()).append("\n")
                .append("  Miss Rate: ").append(cacheStats.missRate()).append("\n")
                .append("  Load Success: ").append(cacheStats.loadSuccessCount()).append("\n")
                .append("  Load Failure: ").append(cacheStats.loadFailureCount()).append("\n\n");
        });
        
        return stats.toString();
    }
}