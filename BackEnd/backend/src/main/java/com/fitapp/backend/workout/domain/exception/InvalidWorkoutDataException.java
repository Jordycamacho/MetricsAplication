package com.fitapp.backend.workout.domain.exception;

public class InvalidWorkoutDataException extends RuntimeException {

    private InvalidWorkoutDataException(String message) {
        super(message);
    }

    public static InvalidWorkoutDataException emptySetExecutions() {
        return new InvalidWorkoutDataException("A workout session must contain at least one set execution.");
    }

    public static InvalidWorkoutDataException invalidTimeRange() {
        return new InvalidWorkoutDataException("End time must be after start time.");
    }

    public static InvalidWorkoutDataException missingParameterValue() {
        return new InvalidWorkoutDataException("Each parameter must have at least one value populated.");
    }

    public static InvalidWorkoutDataException missingExerciseId() {
        return new InvalidWorkoutDataException("Each set execution must reference a valid exercise ID.");
    }
}