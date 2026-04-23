package com.fitapp.backend.auth.domain.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("Usuario no encontrado con ID: " + userId);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}