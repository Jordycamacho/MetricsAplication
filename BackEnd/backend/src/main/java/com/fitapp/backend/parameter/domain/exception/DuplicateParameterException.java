package com.fitapp.backend.parameter.domain.exception;

public class DuplicateParameterException extends RuntimeException {
    public DuplicateParameterException(String message) { super(message); }
}