package com.fitapp.backend.application.logging;

import java.util.UUID;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RoutineServiceLogger {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final ThreadLocal<String> correlationId = new ThreadLocal<>();
    
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
    
    private String getCorrelationId() {
        String id = correlationId.get();
        if (id == null) {
            id = UUID.randomUUID().toString();
            correlationId.set(id);
        }
        return id;
    }
}