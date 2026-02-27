package com.fitapp.backend.domain.exception;

public class CategoryDuplicateException extends DomainException {

    public CategoryDuplicateException(String name, String userEmail) {
        super("Ya existe una categoría con el nombre '" + name + "'", "CATEGORY_DUPLICATE");
        withContext("name", name);
        withContext("user", userEmail);
    }
}