package com.fitapp.backend.workout.domain.exception;

public class WorkoutSessionNotFoundException extends RuntimeException {

    public WorkoutSessionNotFoundException(Long sessionId, Long userId) {
        super(String.format("Workout session not found: sessionId=%d, userId=%d", sessionId, userId));
    }

    public WorkoutSessionNotFoundException(Long sessionId) {
        super(String.format("Workout session not found: sessionId=%d", sessionId));
    }
}