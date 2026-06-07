package com.fitapp.backend.notification.domain.exception;

public class EmailRateLimitExceededException extends RuntimeException {
    public EmailRateLimitExceededException() {
        super("Demasiadas solicitudes. Inténtalo de nuevo más tarde.");
    }
}
