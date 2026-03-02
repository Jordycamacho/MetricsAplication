package com.fitapp.backend.domain.exception;

public class SetTemplatePositionConflictException extends RuntimeException {
    public SetTemplatePositionConflictException(Integer position, Long routineExerciseId) {
        super("Position " + position + " is already taken for routine exercise " + routineExerciseId);
    }
}