package com.fitapp.backend.category.domain.exception;

import com.fitapp.backend.infrastructure.shared.exception.DomainException;

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