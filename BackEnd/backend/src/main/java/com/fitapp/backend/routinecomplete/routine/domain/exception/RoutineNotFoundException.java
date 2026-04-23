package com.fitapp.backend.routinecomplete.routine.domain.exception;

public class RoutineNotFoundException extends RuntimeException {
    public RoutineNotFoundException(Long id) {
        super("Routine not found with id: " + id);
    }

    public RoutineNotFoundException(String message) {
        super("Routine not found with id: " + message);
    }
}
