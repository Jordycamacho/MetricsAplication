package com.fitapp.appfit.feature.routine.util

object RoutineValidation {

    /**
     * Valida el nombre de la rutina
     */
    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "El nombre no puede estar vacío"
            name.length < 3 -> "El nombre debe tener al menos 3 caracteres"
            name.length > 100 -> "El nombre no puede exceder 100 caracteres"
            else -> null
        }
    }

    /**
     * Valida la descripción
     */
    fun validateDescription(description: String?): String? {
        if (description.isNullOrBlank()) return null
        return when {
            description.length > 500 -> "La descripción no puede exceder 500 caracteres"
            else -> null
        }
    }

    /**
     * Valida el objetivo
     */
    fun validateGoal(goal: String): String? {
        return when {
            goal.isBlank() -> "El objetivo no puede estar vacío"
            goal.length > 200 -> "El objetivo no puede exceder 200 caracteres"
            else -> null
        }
    }

    /**
     * Valida sesiones por semana
     */
    fun validateSessionsPerWeek(sessions: Int?): String? {
        if (sessions == null) return "Las sesiones por semana son obligatorias"
        return when {
            sessions < 1 -> "Debe haber al menos 1 sesión por semana"
            sessions > 7 -> "No puede haber más de 7 sesiones por semana"
            else -> null
        }
    }

    /**
     * Valida días de entrenamiento
     */
    fun validateTrainingDays(days: List<String>): String? {
        return when {
            days.isEmpty() -> "Debes seleccionar al menos un día de entrenamiento"
            days.size > 7 -> "No puedes seleccionar más de 7 días"
            else -> null
        }
    }

    /**
     * Valida versión (formato semver opcional)
     */
    fun validateVersion(version: String?): String? {
        if (version.isNullOrBlank()) return null
        return when {
            version.length > 20 -> "La versión no puede exceder 20 caracteres"
            else -> null
        }
    }

    /**
     * Valida todo el formulario de creación
     */
    fun validateCreateForm(
        name: String,
        description: String?,
        goal: String,
        sessionsPerWeek: Int?,
        trainingDays: List<String>,
        version: String? = null
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        validateName(name)?.let { errors["name"] = it }
        validateDescription(description)?.let { errors["description"] = it }
        validateGoal(goal)?.let { errors["goal"] = it }
        validateSessionsPerWeek(sessionsPerWeek)?.let { errors["sessionsPerWeek"] = it }
        validateTrainingDays(trainingDays)?.let { errors["trainingDays"] = it }
        validateVersion(version)?.let { errors["version"] = it }

        return errors
    }

    /**
     * Valida formulario de actualización
     */
    fun validateUpdateForm(
        name: String?,
        description: String?,
        goal: String?,
        sessionsPerWeek: Int?,
        trainingDays: List<String>?,
        version: String? = null
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        name?.let { validateName(it)?.let { err -> errors["name"] = err } }
        description?.let { validateDescription(it)?.let { err -> errors["description"] = err } }
        goal?.let { validateGoal(it)?.let { err -> errors["goal"] = err } }
        sessionsPerWeek?.let { validateSessionsPerWeek(it)?.let { err -> errors["sessionsPerWeek"] = err } }
        trainingDays?.let { validateTrainingDays(it)?.let { err -> errors["trainingDays"] = err } }
        version?.let { validateVersion(it)?.let { err -> errors["version"] = err } }

        return errors
    }
}