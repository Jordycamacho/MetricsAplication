package com.fitapp.backend.auth.domain.exception;

class JwtAuthenticationException extends RuntimeException {
        public JwtAuthenticationException(String message) {
            super(message);
        }
    }