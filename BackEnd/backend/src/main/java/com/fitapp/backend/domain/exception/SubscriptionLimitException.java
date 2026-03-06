package com.fitapp.backend.domain.exception;

public class SubscriptionLimitException extends RuntimeException {
    public SubscriptionLimitException(String message) {
        super(message);
    }
}