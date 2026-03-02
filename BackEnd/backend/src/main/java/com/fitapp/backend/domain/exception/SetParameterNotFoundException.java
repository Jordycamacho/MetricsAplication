package com.fitapp.backend.domain.exception;

public class SetParameterNotFoundException extends RuntimeException {
    public SetParameterNotFoundException(Long id) {
        super("Set parameter not found with id: " + id);
    }
}