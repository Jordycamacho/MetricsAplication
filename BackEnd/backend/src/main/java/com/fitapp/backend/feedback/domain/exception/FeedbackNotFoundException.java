package com.fitapp.backend.feedback.domain.exception;

public class FeedbackNotFoundException extends RuntimeException {
    public FeedbackNotFoundException(Long id) {
        super("Feedback not found with id: " + id);
    }
}
