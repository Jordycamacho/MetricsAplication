package com.fitapp.appfit.feature.exercise.util

import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseType

object ExerciseValidation {

    /**
     * Tipos de ejercicio con labels legibles
     */
    enum class ExerciseTypeInfo(val type: ExerciseType, val label: String, val description: String) {
        SIMPLE(ExerciseType.SIMPLE, "Simple", "Ejercicio básico sin parámetros especiales"),
        WEIGHTED(ExerciseType.WEIGHTED, "Con peso", "Ejercicio que usa pesos (kg, lb)"),
        TIMED(ExerciseType.TIMED, "Por tiempo", "Ejercicio basado en duración"),
        MIXED(ExerciseType.MIXED, "Mixto", "Combina peso, tiempo y repeticiones"),
        BODYWEIGHT(ExerciseType.BODYWEIGHT, "Peso corporal", "Solo usa el peso del cuerpo"),
        DISTANCE(ExerciseType.DISTANCE, "Por distancia", "Ejercicio basado en distancia (km, m)"),
        REPETITION(ExerciseType.REPETITION, "Por repeticiones", "Ejercicio basado en número de reps"),
        DURATION(ExerciseType.DURATION, "Por duración", "Ejercicio de tiempo sostenido"),
        CIRCUIT(ExerciseType.CIRCUIT, "Circuito", "Serie de ejercicios en secuencia"),
        AMRAP(ExerciseType.AMRAP, "AMRAP", "As Many Reps As Possible"),
        EMOM(ExerciseType.EMOM, "EMOM", "Every Minute On the Minute"),
        TABATA(ExerciseType.TABATA, "Tabata", "20s trabajo / 10s descanso");

        companion object {
            fun fromType(type: ExerciseType): ExerciseTypeInfo? {
                return values().find { it.type == type }
            }

            fun getAllLabels(): List<String> = values().map { it.label }
            fun getAllTypes(): List<ExerciseType> = values().map { it.type }
        }
    }

    /**
     * Valida el nombre del ejercicio
     */
    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "El nombre no puede estar vacío"
            name.length < 3 -> "El nombre debe tener al menos 3 caracteres"
            name.length > 100 -> "El nombre no puede exceder 100 caracteres"
            !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s]+$")) ->
                "El nombre solo puede contener letras, números y espacios"
            else -> null
        }
    }

    /**
     * Valida la descripción
     */
    fun validateDescription(description: String?): String? {
        return if (description != null && description.length > 500) {
            "La descripción no puede exceder 500 caracteres"
        } else null
    }

    /**
     * Valida que al menos un deporte esté seleccionado
     */
    fun validateSports(sportIds: Set<Long>): String? {
        return when {
            sportIds.isEmpty() -> "Debes seleccionar al menos un deporte"
            sportIds.size > 5 -> "No puedes seleccionar más de 5 deportes"
            else -> null
        }
    }

    /**
     * Valida parámetros soportados
     */
    fun validateParameters(parameterIds: Set<Long>, exerciseType: ExerciseType): String? {
        return when {
            parameterIds.isEmpty() -> "Advertencia: Sin parámetros, el ejercicio será básico"
            parameterIds.size > 10 -> "Advertencia: Más de 10 parámetros puede ser confuso"
            exerciseType == ExerciseType.SIMPLE && parameterIds.size > 3 ->
                "Advertencia: Ejercicios SIMPLE típicamente tienen 1-3 parámetros"
            else -> null
        }
    }

    /**
     * Obtiene parámetros sugeridos según el tipo de ejercicio
     */
    fun getSuggestedParametersMessage(exerciseType: ExerciseType): String {
        return when (exerciseType) {
            ExerciseType.WEIGHTED -> "Sugerido: peso, repeticiones"
            ExerciseType.TIMED -> "Sugerido: duración, intensidad"
            ExerciseType.DISTANCE -> "Sugerido: distancia, tiempo"
            ExerciseType.BODYWEIGHT -> "Sugerido: repeticiones, series"
            ExerciseType.DURATION -> "Sugerido: duración total"
            ExerciseType.REPETITION -> "Sugerido: número de repeticiones"
            ExerciseType.CIRCUIT, ExerciseType.AMRAP, ExerciseType.EMOM, ExerciseType.TABATA ->
                "Sugerido: rondas, tiempo por ronda"
            ExerciseType.MIXED -> "Sugerido: peso, repeticiones, tiempo"
            else -> "Sin sugerencias específicas"
        }
    }

    /**
     * Determina si el ejercicio debe ser público por defecto según el tipo
     */
    fun shouldBePublicByDefault(exerciseType: ExerciseType): Boolean {
        // Ejercicios especializados típicamente son personales
        return exerciseType in listOf(
            ExerciseType.SIMPLE,
            ExerciseType.WEIGHTED,
            ExerciseType.BODYWEIGHT
        )
    }

    /**
     * Obtiene mensaje de ayuda para el tipo de ejercicio
     */
    fun getTypeHelpMessage(exerciseType: ExerciseType): String {
        return when (exerciseType) {
            ExerciseType.SIMPLE -> "Ejercicio básico, ideal para principiantes"
            ExerciseType.WEIGHTED -> "Requiere equipo con peso ajustable"
            ExerciseType.BODYWEIGHT -> "No requiere equipo, solo tu cuerpo"
            ExerciseType.TIMED -> "Se mide por cuánto tiempo lo mantienes"
            ExerciseType.DISTANCE -> "Se mide por distancia recorrida"
            ExerciseType.AMRAP -> "Completa tantas rondas como puedas en el tiempo dado"
            ExerciseType.EMOM -> "Completa el trabajo al inicio de cada minuto"
            ExerciseType.TABATA -> "8 rondas de 20s trabajo / 10s descanso"
            ExerciseType.CIRCUIT -> "Serie de ejercicios realizados en secuencia"
            else -> "Tipo avanzado de ejercicio"
        }
    }

    /**
     * Valida rating
     */
    fun validateRating(rating: Double): String? {
        return when {
            rating < 0.0 -> "El rating no puede ser negativo"
            rating > 5.0 -> "El rating no puede ser mayor a 5"
            else -> null
        }
    }

    /**
     * Formatea el rating para mostrar
     */
    fun formatRating(rating: Double?, ratingCount: Int?): String {
        return if (rating != null && ratingCount != null && ratingCount > 0) {
            String.format("%.1f  (%d)", rating, ratingCount)
        } else {
            "Sin calificación"
        }
    }

    /**
     * Determina el color del badge según visibilidad
     */
    fun getVisibilityColor(isPublic: Boolean): String {
        return if (isPublic) "#78703F" else "#B3B3B3"
    }

    /**
     * Obtiene el label de visibilidad
     */
    fun getVisibilityLabel(isPublic: Boolean, isSystem: Boolean = false): String {
        return when {
            isSystem -> "Sistema"
            isPublic -> "Público"
            else -> "Personal"
        }
    }
}