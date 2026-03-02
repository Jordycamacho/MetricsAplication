package com.fitapp.backend.infrastructure.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

        @Bean
        public CacheManager cacheManager() {
                CaffeineCacheManager cacheManager = new CaffeineCacheManager();

                cacheManager.setCaffeine(Caffeine.newBuilder()
                                .expireAfterWrite(10, TimeUnit.MINUTES)
                                .maximumSize(100)
                                .recordStats());

                // ── Routines ──────────────────────────────────────────────────────────
                cacheManager.registerCustomCache("routines", Caffeine.newBuilder()
                                .expireAfterWrite(10, TimeUnit.MINUTES)
                                .maximumSize(100)
                                .recordStats()
                                .build());

                cacheManager.registerCustomCache("userRoutines", Caffeine.newBuilder()
                                .expireAfterWrite(5, TimeUnit.MINUTES)
                                .maximumSize(50)
                                .recordStats()
                                .build());

                cacheManager.registerCustomCache("recentRoutines", Caffeine.newBuilder()
                                .expireAfterWrite(2, TimeUnit.MINUTES)
                                .maximumSize(20)
                                .build());

                cacheManager.registerCustomCache("activeRoutines", Caffeine.newBuilder()
                                .expireAfterWrite(15, TimeUnit.MINUTES)
                                .maximumSize(20)
                                .build());

                cacheManager.registerCustomCache("routineStats", Caffeine.newBuilder()
                                .expireAfterWrite(30, TimeUnit.MINUTES)
                                .maximumSize(10)
                                .build());

                // ── Sports ────────────────────────────────────────────────────────────
                cacheManager.registerCustomCache("predefined-sports",
                                Caffeine.newBuilder()
                                                .expireAfterWrite(24, TimeUnit.HOURS)
                                                .maximumSize(200)
                                                .recordStats()
                                                .build());

                cacheManager.registerCustomCache("user-sports",
                                Caffeine.newBuilder()
                                                .expireAfterWrite(15, TimeUnit.MINUTES)
                                                .maximumSize(1000)
                                                .recordStats()
                                                .build());

                cacheManager.registerCustomCache("sport-by-id",
                                Caffeine.newBuilder()
                                                .expireAfterWrite(30, TimeUnit.MINUTES)
                                                .maximumSize(2000)
                                                .recordStats()
                                                .build());

                // ── Categories ────────────────────────────────────────────────────────
                cacheManager.registerCustomCache("predefined-categories",
                                Caffeine.newBuilder()
                                                .expireAfterWrite(24, TimeUnit.HOURS)
                                                .maximumSize(500)
                                                .recordStats()
                                                .build());

                cacheManager.registerCustomCache("user-categories",
                                Caffeine.newBuilder()
                                                .expireAfterWrite(10, TimeUnit.MINUTES)
                                                .maximumSize(1000)
                                                .recordStats()
                                                .build());

                cacheManager.registerCustomCache("available-categories",
                                Caffeine.newBuilder()
                                                .expireAfterWrite(5, TimeUnit.MINUTES)
                                                .maximumSize(1000)
                                                .recordStats()
                                                .build());

                cacheManager.registerCustomCache("category-by-id",
                                Caffeine.newBuilder()
                                                .expireAfterWrite(15, TimeUnit.MINUTES)
                                                .maximumSize(2000)
                                                .recordStats()
                                                .build());

                cacheManager.registerCustomCache("most-used-categories",
                                Caffeine.newBuilder()
                                                .expireAfterWrite(30, TimeUnit.MINUTES)
                                                .maximumSize(50)
                                                .recordStats()
                                                .build());

                // ── Set Templates ─────────────────────────────────────────────────────────
                cacheManager.registerCustomCache("setTemplates",
                                Caffeine.newBuilder()
                                                .expireAfterWrite(5, TimeUnit.MINUTES)
                                                .maximumSize(500)
                                                .recordStats()
                                                .build());

                cacheManager.registerCustomCache("setParameters",
                                Caffeine.newBuilder()
                                                .expireAfterWrite(5, TimeUnit.MINUTES)
                                                .maximumSize(1000)
                                                .recordStats()
                                                .build());

                cacheManager.registerCustomCache("setTemplatesByExercise",
                                Caffeine.newBuilder()
                                                .expireAfterWrite(5, TimeUnit.MINUTES)
                                                .maximumSize(500)
                                                .recordStats()
                                                .build());

                return cacheManager;
        }
}