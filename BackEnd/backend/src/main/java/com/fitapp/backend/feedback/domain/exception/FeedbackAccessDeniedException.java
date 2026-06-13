package com.fitapp.backend.feedback.domain.exception;

public class FeedbackAccessDeniedException extends RuntimeException {
    public FeedbackAccessDeniedException() {
        super("You do not have permission to access this feedback");
    }
}
