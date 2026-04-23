package com.fitapp.backend.category.domain.exception;

import com.fitapp.backend.infrastructure.shared.exception.DomainException;

public class PredefinedCategoryException extends DomainException {

    public PredefinedCategoryException(Long categoryId) {
        super("No se pueden modificar o eliminar categorías predefinidas", "CATEGORY_PREDEFINED");
        withContext("categoryId", categoryId);
    }
}