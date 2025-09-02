package com.fitapp.backend.domain.exception;

public class RoutineNotFoundException extends RuntimeException {
    public RoutineNotFoundException(Long id) {
        super("Routine not found with id: " + id);
    }
}