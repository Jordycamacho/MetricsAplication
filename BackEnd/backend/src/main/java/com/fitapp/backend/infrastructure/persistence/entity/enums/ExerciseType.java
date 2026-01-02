package com.fitapp.backend.infrastructure.persistence.entity.enums;

public enum ExerciseType {
    SIMPLE,          // Solo un parámetro principal
    WEIGHTED,        // Con peso
    TIMED,           // Con tiempo
    MIXED,           // Múltiples parámetros (peso + tiempo)
    BODYWEIGHT,      // Peso corporal
    DISTANCE,        // Distancia
    REPETITION,      // Solo repeticiones
    DURATION,        // Solo duración
    CIRCUIT,         // Circuito
    AMRAP,           // As Many Rounds As Possible
    EMOM,            // Every Minute On the Minute
    TABATA           // Intervalos específicos
}
