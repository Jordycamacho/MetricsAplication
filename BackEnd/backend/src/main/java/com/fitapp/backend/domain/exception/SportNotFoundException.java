package com.fitapp.backend.domain.exception;

public class SportNotFoundException extends RuntimeException {
    public SportNotFoundException(Long id) {
        super("Sport not found with id: " + id);
    }
}