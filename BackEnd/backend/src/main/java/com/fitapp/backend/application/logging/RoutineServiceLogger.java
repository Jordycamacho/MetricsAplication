package com.fitapp.backend.application.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RoutineServiceLogger {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final ThreadLocal<String> correlationId = new ThreadLocal<>();
    
    // --- CRUD Operations ---
    public void logRoutineCreationStart(String userEmail, String routineName) {
        log.info("ROUTINE_CREATION_START | user={} | routine={} | correlationId={}", 
                 userEmail, routineName, getCorrelationId());
    }
    
    public void logRoutineCreationSuccess(Long routineId, String userEmail) {
        log.info("ROUTINE_CREATION_SUCCESS | routineId={} | user={} | correlationId={}", 
                 routineId, userEmail, getCorrelationId());
        log.debug("Routine {} created successfully for user {}", routineId, userEmail);
    }
    
    public void logRoutineCreationError(String userEmail, String error) {
        log.error("ROUTINE_CREATION_ERROR | user={} | error={} | correlationId={}", 
                  userEmail, error, getCorrelationId());
    }
    
    public void logRoutineRetrievalStart(Long routineId, String userEmail) {
        log.debug("ROUTINE_RETRIEVAL_START | routineId={} | user={} | correlationId={}", 
                  routineId, userEmail, getCorrelationId());
    }
    
    public void logRoutineRetrievalSuccess(Long routineId, String userEmail) {
        log.debug("ROUTINE_RETRIEVAL_SUCCESS | routineId={} | user={} | correlationId={}", 
                  routineId, userEmail, getCorrelationId());
    }
    
    public void logRoutineRetrievalError(Long routineId, String userEmail, String error) {
        log.error("ROUTINE_RETRIEVAL_ERROR | routineId={} | user={} | error={} | correlationId={}", 
                  routineId, userEmail, error, getCorrelationId());
    }
    
    public void logRoutineUpdateStart(Long routineId, String userEmail) {
        log.info("ROUTINE_UPDATE_START | routineId={} | user={} | correlationId={}", 
                 routineId, userEmail, getCorrelationId());
    }
    
    public void logRoutineUpdateSuccess(Long routineId, String userEmail) {
        log.info("ROUTINE_UPDATE_SUCCESS | routineId={} | user={} | correlationId={}", 
                 routineId, userEmail, getCorrelationId());
        log.debug("Routine {} updated successfully for user {}", routineId, userEmail);
    }
    
    public void logRoutineUpdateError(Long routineId, String userEmail, String error) {
        log.error("ROUTINE_UPDATE_ERROR | routineId={} | user={} | error={} | correlationId={}", 
                  routineId, userEmail, error, getCorrelationId());
    }
    
    public void logRoutineDeletionStart(Long routineId, String userEmail) {
        log.warn("ROUTINE_DELETION_START | routineId={} | user={} | correlationId={}", 
                 routineId, userEmail, getCorrelationId());
    }
    
    public void logRoutineDeletionSuccess(Long routineId, String userEmail) {
        log.warn("ROUTINE_DELETION_SUCCESS | routineId={} | user={} | correlationId={}", 
                 routineId, userEmail, getCorrelationId());
        log.debug("Routine {} deleted successfully for user {}", routineId, userEmail);
    }
    
    public void logRoutineDeletionError(Long routineId, String userEmail, String error) {
        log.error("ROUTINE_DELETION_ERROR | routineId={} | user={} | error={} | correlationId={}", 
                  routineId, userEmail, error, getCorrelationId());
    }
    
    public void logRoutineStatusToggle(Long routineId, boolean isActive, String userEmail) {
        log.info("ROUTINE_STATUS_TOGGLE | routineId={} | active={} | user={} | correlationId={}", 
                 routineId, isActive, userEmail, getCorrelationId());
    }
    
    // --- List Operations ---
    public void logRoutineListRetrievalStart(String userEmail, String filterType, int page, int size) {
        log.debug("ROUTINE_LIST_START | user={} | filter={} | page={} | size={} | correlationId={}", 
                  userEmail, filterType, page, size, getCorrelationId());
    }
    
    public void logRoutineListRetrievalSuccess(String userEmail, String filterType, long totalElements) {
        log.debug("ROUTINE_LIST_SUCCESS | user={} | filter={} | total={} | correlationId={}", 
                  userEmail, filterType, totalElements, getCorrelationId());
    }
    
    public void logRoutineListRetrievalError(String userEmail, String filterType, String error) {
        log.error("ROUTINE_LIST_ERROR | user={} | filter={} | error={} | correlationId={}", 
                  userEmail, filterType, error, getCorrelationId());
    }
    
    public void logRecentRoutinesRetrievalStart(String userEmail, int limit) {
        log.debug("RECENT_ROUTINES_START | user={} | limit={} | correlationId={}", 
                  userEmail, limit, getCorrelationId());
    }
    
    public void logRecentRoutinesRetrievalSuccess(String userEmail, int count) {
        log.debug("RECENT_ROUTINES_SUCCESS | user={} | count={} | correlationId={}", 
                  userEmail, count, getCorrelationId());
    }
    
    public void logActiveRoutinesRetrievalStart(String userEmail) {
        log.debug("ACTIVE_ROUTINES_START | user={} | correlationId={}", 
                  userEmail, getCorrelationId());
    }
    
    public void logActiveRoutinesRetrievalSuccess(String userEmail, int count) {
        log.debug("ACTIVE_ROUTINES_SUCCESS | user={} | count={} | correlationId={}", 
                  userEmail, count, getCorrelationId());
    }
    
    // --- Statistics Operations ---
    public void logRoutineStatisticsRetrievalStart(String userEmail) {
        log.debug("ROUTINE_STATS_START | user={} | correlationId={}", 
                  userEmail, getCorrelationId());
    }
    
    public void logRoutineStatisticsRetrievalSuccess(String userEmail) {
        log.debug("ROUTINE_STATS_SUCCESS | user={} | correlationId={}", 
                  userEmail, getCorrelationId());
    }
    
    public void logRoutineStatisticsRetrievalError(String userEmail, String error) {
        log.error("ROUTINE_STATS_ERROR | user={} | error={} | correlationId={}", 
                  userEmail, error, getCorrelationId());
    }
    
    // --- Exercises Operations ---
    public void logExercisesAdditionStart(Long routineId, String userEmail, int exerciseCount) {
        log.info("EXERCISES_ADDITION_START | routineId={} | user={} | count={} | correlationId={}", 
                 routineId, userEmail, exerciseCount, getCorrelationId());
    }
    
    public void logExercisesAdditionSuccess(Long routineId, String userEmail, int exerciseCount) {
        log.info("EXERCISES_ADDITION_SUCCESS | routineId={} | user={} | count={} | correlationId={}", 
                 routineId, userEmail, exerciseCount, getCorrelationId());
        log.debug("{} exercises added to routine {} for user {}", exerciseCount, routineId, userEmail);
    }
    
    public void logExercisesAdditionError(Long routineId, String userEmail, String error) {
        log.error("EXERCISES_ADDITION_ERROR | routineId={} | user={} | error={} | correlationId={}", 
                  routineId, userEmail, error, getCorrelationId());
    }
    
    // --- Cache Operations ---
    public void logCacheEvict(String cacheName, String key) {
        log.debug("CACHE_EVICT | cache={} | key={} | correlationId={}", 
                  cacheName, key, getCorrelationId());
    }
    
    public void logCacheHit(String cacheName, String key) {
        log.debug("CACHE_HIT | cache={} | key={} | correlationId={}", 
                  cacheName, key, getCorrelationId());
    }
    
    public void logCacheMiss(String cacheName, String key) {
        log.debug("CACHE_MISS | cache={} | key={} | correlationId={}", 
                  cacheName, key, getCorrelationId());
    }
    
    // --- Performance Logging ---
    public void logOperationDuration(String operation, long durationMs) {
        if (durationMs > 1000) {
            log.warn("OPERATION_SLOW | operation={} | duration={}ms | correlationId={}", 
                     operation, durationMs, getCorrelationId());
        } else if (durationMs > 500) {
            log.info("OPERATION_INFO | operation={} | duration={}ms | correlationId={}", 
                     operation, durationMs, getCorrelationId());
        } else {
            log.debug("OPERATION_FAST | operation={} | duration={}ms | correlationId={}", 
                      operation, durationMs, getCorrelationId());
        }
    }
    
    // --- Error Context ---
    public void logErrorWithContext(String operation, String userEmail, String error, Throwable exception) {
        log.error("ERROR_CONTEXT | operation={} | user={} | error={} | exception={} | correlationId={}", 
                  operation, userEmail, error, exception != null ? exception.getClass().getSimpleName() : "N/A", 
                  getCorrelationId(), exception);
    }
    
    // --- Business Logic Logging ---
    public void logSubscriptionLimitCheck(String userEmail, int currentRoutines, int maxRoutines) {
        log.debug("SUBSCRIPTION_LIMIT_CHECK | user={} | current={} | max={} | correlationId={}", 
                  userEmail, currentRoutines, maxRoutines, getCorrelationId());
    }
    
    public void logTrainingDaysValidation(String userEmail, int daysSelected) {
        log.debug("TRAINING_DAYS_VALIDATION | user={} | days={} | correlationId={}", 
                  userEmail, daysSelected, getCorrelationId());
    }
    
    public void logSportValidation(Long sportId, String userEmail) {
        log.debug("SPORT_VALIDATION | sportId={} | user={} | correlationId={}", 
                  sportId, userEmail, getCorrelationId());
    }
    
    // --- Correlation ID Management ---
    public void setCorrelationId(String id) {
        correlationId.set(id);
    }
    
    public String getCorrelationId() {
        String id = correlationId.get();
        if (id == null) {
            id = UUID.randomUUID().toString();
            correlationId.set(id);
        }
        return id;
    }
    
    public void clearCorrelationId() {
        correlationId.remove();
    }
}