package com.fitapp.backend.sport.domain.exception;

public class PredefinedSportException extends RuntimeException {
    public PredefinedSportException() {
        super("Cannot modify or delete predefined sports");
    }
}