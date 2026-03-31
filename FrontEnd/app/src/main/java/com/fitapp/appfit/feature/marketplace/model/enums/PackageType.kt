package com.fitapp.appfit.feature.marketplace.model.enums

enum class PackageType(val displayName: String) {
    SPORT_PACK("Pack de Deporte"),
    PARAMETER_PACK("Pack de Parámetros"),
    ROUTINE_PACK("Pack de Rutinas"),
    EXERCISE_PACK("Pack de Ejercicios"),
    MIXED("Pack Mixto");

    companion object {
        fun fromString(value: String?): PackageType? {
            return try {
                valueOf(value?.uppercase() ?: return null)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}