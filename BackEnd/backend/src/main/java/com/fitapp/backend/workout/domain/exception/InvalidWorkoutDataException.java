package com.fitapp.backend.workout.domain.exception;

public class InvalidWorkoutDataException extends RuntimeException {
    public InvalidWorkoutDataException(String message) {
        super(message);
    }
    
    public InvalidWorkoutDataException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static InvalidWorkoutDataException emptySetExecutions() {
        return new InvalidWorkoutDataException("Workout session must contain at least one set execution");
    }
    
    public static InvalidWorkoutDataException invalidTimeRange() {
        return new InvalidWorkoutDataException("End time must be after start time");
    }
    
    public static InvalidWorkoutDataException missingParameterValue() {
        return new InvalidWorkoutDataException("Parameter must have at least one value populated");
    }
}