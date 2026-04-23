package com.fitapp.backend.category.domain.exception;

import com.fitapp.backend.infrastructure.shared.exception.DomainException;

public class CategoryDuplicateException extends DomainException {

    public CategoryDuplicateException(String name, String userEmail) {
        super("Ya existe una categoría con el nombre '" + name + "'", "CATEGORY_DUPLICATE");
        withContext("name", name);
        withContext("user", userEmail);
    }
}