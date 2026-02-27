package com.fitapp.backend.domain.exception;

public class PredefinedCategoryException extends DomainException {

    public PredefinedCategoryException(Long categoryId) {
        super("No se pueden modificar o eliminar categorías predefinidas", "CATEGORY_PREDEFINED");
        withContext("categoryId", categoryId);
    }
}