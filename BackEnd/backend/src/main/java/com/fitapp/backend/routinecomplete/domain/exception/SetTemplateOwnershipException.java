package com.fitapp.backend.routinecomplete.domain.exception;

public class SetTemplateOwnershipException extends RuntimeException {
    public SetTemplateOwnershipException(Long userId, Long setTemplateId) {
        super("User " + userId + " does not own set template " + setTemplateId);
    }
}