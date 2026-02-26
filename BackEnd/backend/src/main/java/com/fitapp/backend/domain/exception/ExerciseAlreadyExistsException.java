package com.fitapp.backend.domain.exception;

public class ExerciseAlreadyExistsException extends RuntimeException {
    public ExerciseAlreadyExistsException(String name) {
        super("You already have an exercise named: " + name);
    }
}