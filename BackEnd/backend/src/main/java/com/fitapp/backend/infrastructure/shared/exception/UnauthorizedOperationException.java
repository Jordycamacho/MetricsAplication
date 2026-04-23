package com.fitapp.backend.infrastructure.shared.exception;

public class UnauthorizedOperationException extends RuntimeException {
    public UnauthorizedOperationException(String message) { super(message); }
}