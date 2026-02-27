package com.fitapp.backend.domain.exception;

public class CategoryNotFoundException extends DomainException {

    public CategoryNotFoundException(Long id) {
        super("Categoría no encontrada con id: " + id, "CATEGORY_NOT_FOUND");
        withContext("categoryId", id);
    }

    public CategoryNotFoundException(String name) {
        super("Categoría no encontrada con nombre: " + name, "CATEGORY_NOT_FOUND");
        withContext("name", name);
    }
}