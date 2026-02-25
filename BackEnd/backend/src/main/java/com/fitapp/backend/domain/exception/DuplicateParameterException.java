package com.fitapp.backend.domain.exception;

public class DuplicateParameterException extends RuntimeException {
    public DuplicateParameterException(String message) { super(message); }
}