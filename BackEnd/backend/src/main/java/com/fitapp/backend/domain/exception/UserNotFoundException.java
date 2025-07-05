package com.fitapp.backend.domain.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID userId) {
        super("Usuario no encontrado con ID: " + userId);
    }

    public UserNotFoundException(String supabaseUid) {
        super("Usuario no encontrado con UID: " + supabaseUid);
    }
}