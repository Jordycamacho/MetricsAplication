package com.fitapp.backend.domain.exception;

public class UnsupportedParameterException extends RuntimeException {
    public UnsupportedParameterException(Long parameterId, Long exerciseId) {
        super("Parameter " + parameterId + " is not supported by exercise " + exerciseId);
    }
}