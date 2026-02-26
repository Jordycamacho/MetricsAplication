package com.fitapp.backend.domain.exception;

public class SportOwnershipException extends RuntimeException {
    public SportOwnershipException(Long sportId) {
        super("You don't own sport with id: " + sportId);
    }
}