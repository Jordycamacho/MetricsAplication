package com.fitapp.backend.Exercise.domain.exception;

public class ExerciseOwnershipException extends RuntimeException {
    public ExerciseOwnershipException(Long exerciseId) {
        super("You don't have permission to modify exercise with id: " + exerciseId);
    }
}