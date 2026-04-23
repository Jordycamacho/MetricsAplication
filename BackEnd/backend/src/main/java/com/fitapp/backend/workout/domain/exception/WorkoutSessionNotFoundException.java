package com.fitapp.backend.workout.domain.exception;

public class WorkoutSessionNotFoundException extends RuntimeException {
    public WorkoutSessionNotFoundException(String message) {
        super(message);
    }
    
    public WorkoutSessionNotFoundException(Long sessionId) {
        super("Workout session not found with id: " + sessionId);
    }
    
    public WorkoutSessionNotFoundException(Long sessionId, Long userId) {
        super("Workout session not found with id: " + sessionId + " for user: " + userId);
    }
}