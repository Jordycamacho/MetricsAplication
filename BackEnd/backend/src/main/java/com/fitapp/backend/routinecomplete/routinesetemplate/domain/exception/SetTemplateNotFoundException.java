package com.fitapp.backend.routinecomplete.routinesetemplate.domain.exception;

public class SetTemplateNotFoundException extends RuntimeException {
    public SetTemplateNotFoundException(Long id) {
        super("Set template not found with id: " + id);
    }
}