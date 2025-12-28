package com.fitapp.backend.application.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class ParameterServiceLogger {
    
    private static final String SERVICE = "PARAMETER_SERVICE";
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
    
    // Métodos específicos
    public void logParameterCreationStart(String userEmail, String parameterName) {
        log.info("PARAMETER_CREATION_START | user={} | parameter={} | correlationId={}", 
                userEmail, parameterName, getCorrelationId());
    }
    
    public void logParameterCreationSuccess(Long parameterId, String userEmail) {
        log.info("PARAMETER_CREATION_SUCCESS | parameterId={} | user={} | correlationId={}", 
                parameterId, userEmail, getCorrelationId());
    }
    
    public void logParameterUpdateStart(Long parameterId, String userEmail) {
        log.info("PARAMETER_UPDATE_START | parameterId={} | user={} | correlationId={}", 
                parameterId, userEmail, getCorrelationId());
    }
    
    public void logParameterUpdateSuccess(Long parameterId, String userEmail) {
        log.info("PARAMETER_UPDATE_SUCCESS | parameterId={} | user={} | correlationId={}", 
                parameterId, userEmail, getCorrelationId());
    }
    
    public void logParameterDeletionStart(Long parameterId, String userEmail) {
        log.warn("PARAMETER_DELETION_START | parameterId={} | user={} | correlationId={}", 
                parameterId, userEmail, getCorrelationId());
    }
    
    public void logParameterDeletionSuccess(Long parameterId, String userEmail) {
        log.warn("PARAMETER_DELETION_SUCCESS | parameterId={} | user={} | correlationId={}", 
                parameterId, userEmail, getCorrelationId());
    }
    
    public void logParameterRetrieval(String userEmail, int count, String type) {
        log.info("PARAMETER_RETRIEVAL | user={} | count={} | type={} | correlationId={}", 
                userEmail, count, type, getCorrelationId());
    }
    
    public void logParameterUsageIncrement(Long parameterId, int newCount) {
        log.debug("PARAMETER_USAGE_INCREMENT | parameterId={} | newCount={} | correlationId={}", 
                parameterId, newCount, getCorrelationId());
    }
    
    public void logDataFormatCheck(String field, Object value, String format) {
        log.debug("PARAMETER_FORMAT_CHECK | field={} | value={} | format={} | correlationId={}", 
                field, value, format, getCorrelationId());
    }
    
    public void logParameterValidation(String parameterName, Object value, boolean isValid) {
        log.debug("PARAMETER_VALIDATION | parameter={} | value={} | isValid={} | correlationId={}", 
                parameterName, value, isValid, getCorrelationId());
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