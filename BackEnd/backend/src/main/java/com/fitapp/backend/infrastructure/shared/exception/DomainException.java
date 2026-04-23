package com.fitapp.backend.infrastructure.shared.exception;

import java.util.HashMap;
import java.util.Map;

public abstract class DomainException extends RuntimeException {
    private final String errorCode;
    private final Map<String, Object> context;
    
    public DomainException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
    }
    
    public DomainException withContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }
}