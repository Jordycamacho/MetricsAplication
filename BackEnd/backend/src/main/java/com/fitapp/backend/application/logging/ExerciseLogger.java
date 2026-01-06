package com.fitapp.backend.application.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class ExerciseLogger {
    
    private static final String SERVICE = "EXERCISE_SERVICE";
    private static final ThreadLocal<String> correlationId = ThreadLocal.withInitial(() -> UUID.randomUUID().toString());
    
    public void logServiceEntry(String method, Object... params) {
        log.info("{}_ENTRY | method={} | params={} | correlationId={}", 
                SERVICE, method, formatParams(params), getCorrelationId());
    }
    
    public void logServiceExit(String method, Object result) {
        log.info("{}_EXIT | method={} | resultType={} | correlationId={}", 
                SERVICE, method, result != null ? result.getClass().getSimpleName() : "null", 
                getCorrelationId());
    }
    
    public void logServiceError(String method, String error, Exception e) {
        log.error("{}_ERROR | method={} | error={} | exception={} | correlationId={}", 
                SERVICE, method, error, e.getClass().getSimpleName(), getCorrelationId(), e);
    }
    
    public void logExerciseCreation(String userEmail, Long exerciseId, String exerciseName) {
        log.info("EXERCISE_CREATED | user={} | exerciseId={} | name={} | correlationId={}", 
                userEmail, exerciseId, exerciseName, getCorrelationId());
    }
    
    public void logExerciseUpdate(String userEmail, Long exerciseId, String exerciseName) {
        log.info("EXERCISE_UPDATED | user={} | exerciseId={} | name={} | correlationId={}", 
                userEmail, exerciseId, exerciseName, getCorrelationId());
    }
    
    public void logExerciseDeletion(String userEmail, Long exerciseId) {
        log.info("EXERCISE_DELETED | user={} | exerciseId={} | correlationId={}", 
                userEmail, exerciseId, getCorrelationId());
    }
    
    public void logExerciseDeactivation(String userEmail, Long exerciseId) {
        log.info("EXERCISE_DEACTIVATED | user={} | exerciseId={} | correlationId={}", 
                userEmail, exerciseId, getCorrelationId());
    }
    
    public void logExerciseRetrieval(String userEmail, int count, String context) {
        log.info("EXERCISES_RETRIEVED | user={} | count={} | context={} | correlationId={}", 
                userEmail, count, context, getCorrelationId());
    }
    
    public void logExerciseDetailAccess(String userEmail, Long exerciseId, String exerciseName) {
        log.info("EXERCISE_DETAIL_ACCESS | user={} | exerciseId={} | name={} | correlationId={}", 
                userEmail, exerciseId, exerciseName, getCorrelationId());
    }
    
    public void logExerciseStatusToggle(String userEmail, Long exerciseId, Boolean newStatus) {
        log.info("EXERCISE_STATUS_TOGGLED | user={} | exerciseId={} | newStatus={} | correlationId={}", 
                userEmail, exerciseId, newStatus, getCorrelationId());
    }
    
    public void logExerciseUsageIncrement(Long exerciseId) {
        log.debug("EXERCISE_USAGE_INCREMENTED | exerciseId={} | correlationId={}", 
                exerciseId, getCorrelationId());
    }
    
    public void logExerciseRating(Long exerciseId, Double rating, String userEmail) {
        log.info("EXERCISE_RATED | user={} | exerciseId={} | rating={} | correlationId={}", 
                userEmail, exerciseId, rating, getCorrelationId());
    }
    
    public void logExerciseDuplication(String userEmail, Long originalId, Long copyId) {
        log.info("EXERCISE_DUPLICATED | user={} | originalId={} | copyId={} | correlationId={}", 
                userEmail, originalId, copyId, getCorrelationId());
    }
    
    public void logExercisePublish(String userEmail, Long exerciseId) {
        log.info("EXERCISE_PUBLISHED | user={} | exerciseId={} | correlationId={}", 
                userEmail, exerciseId, getCorrelationId());
    }
    
    public void logCleanup(int count, int days) {
        log.info("EXERCISE_CLEANUP | count={} | daysInactive={} | correlationId={}", 
                count, days, getCorrelationId());
    }
    
    private String formatParams(Object[] params) {
        if (params == null || params.length == 0) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(", ");
            
            Object param = params[i];
            if (param instanceof String) {
                sb.append('"').append(param).append('"');
            } else {
                sb.append(param);
            }
        }
        sb.append("]");
        
        return sb.toString();
    }
    
    private String getCorrelationId() {
        return correlationId.get();
    }
    
    public void setCorrelationId(String id) {
        correlationId.set(id);
    }
    
    public void clearCorrelationId() {
        correlationId.remove();
    }
}