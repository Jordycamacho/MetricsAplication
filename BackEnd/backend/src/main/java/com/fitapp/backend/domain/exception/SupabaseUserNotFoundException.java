package com.fitapp.backend.domain.exception;

public class SupabaseUserNotFoundException extends RuntimeException {
    public SupabaseUserNotFoundException(String uid) {
        super("Usuario con UID: " + uid + " no encontrado en Supabase Auth");
    }
}