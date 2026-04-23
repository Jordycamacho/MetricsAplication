package com.fitapp.backend.sport.aplication.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.fitapp.backend.infrastructure.persistence.entity.enums.SportSourceType;
import java.util.UUID;

@Component
@Slf4j
public class SportServiceLogger {
    
    private static final String SERVICE = "SPORT_SERVICE";
    private static final ThreadLocal<String> correlationId = new ThreadLocal<>();
    
    // Métodos generales
    public void logServiceEntry(String method, Object... params) {
        log.info("{}_ENTRY | method={} | params={} | correlationId={}", 
                SERVICE, method, params, getCorrelationId());
    }
    
    public void logServiceExit(String method, Object result) {
        log.info("{}_EXIT | method={} | result={} | correlationId={}", 
                SERVICE, method, result, getCorrelationId());
    }
    
    public void logServiceError(String method, String error, Exception e) {
        log.error("{}_ERROR | method={} | error={} | exception={} | correlationId={}", 
                SERVICE, method, error, e.getMessage(), getCorrelationId(), e);
    }
    
    // Métodos específicos para deportes
    public void logSportCreationStart(String userEmail, String sportName, SportSourceType sourceType) {
        log.info("SPORT_CREATION_START | user={} | sport={} | sourceType={} | correlationId={}", 
                userEmail, sportName, sourceType, getCorrelationId());
    }
    
    public void logSportCreationSuccess(Long sportId, String userEmail, SportSourceType sourceType) {
        log.info("SPORT_CREATION_SUCCESS | sportId={} | user={} | sourceType={} | correlationId={}", 
                sportId, userEmail, sourceType, getCorrelationId());
    }
    
    public void logSportRetrieval(String userEmail, int count, String type) {
        log.info("SPORT_RETRIEVAL | user={} | count={} | type={} | correlationId={}", 
                userEmail, count, type, getCorrelationId());
    }
    
    public void logSportDeletionStart(Long sportId, String userEmail) {
        log.warn("SPORT_DELETION_START | sportId={} | user={} | correlationId={}", 
                sportId, userEmail, getCorrelationId());
    }
    
    public void logSportDeletionSuccess(Long sportId, String userEmail) {
        log.warn("SPORT_DELETION_SUCCESS | sportId={} | user={} | correlationId={}", 
                sportId, userEmail, getCorrelationId());
    }
    
    public void logDataFormatCheck(String field, Object value, String format) {
        log.debug("DATA_FORMAT_CHECK | field={} | value={} | format={} | correlationId={}", 
                field, value, format, getCorrelationId());
    }
    
    // Manejo de correlation ID
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