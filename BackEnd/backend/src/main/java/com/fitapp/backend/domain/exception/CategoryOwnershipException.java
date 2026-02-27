package com.fitapp.backend.domain.exception;

public class CategoryOwnershipException extends DomainException {

    public CategoryOwnershipException(Long categoryId, String userEmail) {
        super("No tienes permisos para modificar la categoría con id: " + categoryId, "CATEGORY_FORBIDDEN");
        withContext("categoryId", categoryId);
        withContext("user", userEmail);
    }
}