package com.fitapp.backend.infrastructure.persistence.entity.enums;

public enum PackageType {
    SPORT_PACK,      // deportes + sus parámetros predefinidos + categorías
    PARAMETER_PACK,  // solo parámetros custom
    ROUTINE_PACK,    // rutinas completas (incluye ejercicios si son custom)
    EXERCISE_PACK,   // ejercicios custom con sus parámetros
    MIXED            // combinación de varios tipos
}