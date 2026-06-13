package com.fitapp.backend.notification.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailType {
    VERIFY_EMAIL("email/verify-email", "Verifica tu correo - JNOBFIT", EmailCategory.TRANSACTIONAL),
    WELCOME("email/welcome", "¡Bienvenido a JNOBFIT!", EmailCategory.TRANSACTIONAL),
    PASSWORD_RESET("email/password-reset", "Restablece tu contraseña - JNOBFIT", EmailCategory.TRANSACTIONAL),
    ACCOUNT_DELETED("email/account-deleted", "Tu cuenta en JNOBFIT ha sido eliminada", EmailCategory.TRANSACTIONAL),
    PASSWORD_CHANGED("email/password-changed", "Tu contraseña ha sido cambiada - JNOBFIT", EmailCategory.TRANSACTIONAL),
    FEEDBACK_RECEIVED_USER("email/feedback-received-user", "Hemos recibido tu envío — JNOBFIT", EmailCategory.TRANSACTIONAL),
    FEEDBACK_RECEIVED_ADMIN("email/feedback-received-admin", "Nuevo feedback recibido — JNOBFIT", EmailCategory.TRANSACTIONAL);

    private final String templateName;
    private final String defaultSubject;
    private final EmailCategory category;
}
