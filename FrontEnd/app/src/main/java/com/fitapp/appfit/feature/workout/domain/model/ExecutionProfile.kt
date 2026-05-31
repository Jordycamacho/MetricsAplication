package com.fitapp.appfit.feature.workout.domain.model

/**
 * Perfiles de vista de ejecución (post-v1).
 *
 * - SETS: vista actual día → ejercicio → sets (gym, fuerza).
 * - INTERVALS: timer central, fases trabajo/descanso (Tabata, EMOM, AMRAP).
 * - SEGMENTS: bloques lineales con distancia/tiempo (running, natación).
 * - CIRCUIT: estaciones en circuito con contador de rondas.
 */
enum class ExecutionProfile {
    SETS,
    INTERVALS,
    SEGMENTS,
    CIRCUIT
}
