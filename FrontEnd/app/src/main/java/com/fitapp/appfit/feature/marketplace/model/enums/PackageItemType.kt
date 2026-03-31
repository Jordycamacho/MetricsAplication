package com.fitapp.appfit.feature.marketplace.model.enums

enum class PackageItemType(val displayName: String) {
    SPORT("Deporte"),
    PARAMETER("Parámetro"),
    ROUTINE("Rutina"),
    EXERCISE("Ejercicio"),
    CATEGORY("Categoría");

    companion object {
        fun fromString(value: String?): PackageItemType? {
            return try {
                valueOf(value?.uppercase() ?: return null)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}