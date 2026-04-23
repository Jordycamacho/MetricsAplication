package com.fitapp.backend.suscription.domain.exception;

public class SubscriptionLimitException extends RuntimeException {
    public SubscriptionLimitException(String message) {
        super(message);
    }
}