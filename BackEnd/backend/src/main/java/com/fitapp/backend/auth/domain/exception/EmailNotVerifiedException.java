package com.fitapp.backend.auth.domain.exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("Debes verificar tu correo electrónico antes de iniciar sesión");
    }
}
