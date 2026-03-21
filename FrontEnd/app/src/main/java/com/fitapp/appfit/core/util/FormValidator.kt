package com.fitapp.appfit.core.util

object FormValidator {

    fun validateRoutineName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("El nombre es obligatorio")
            name.length < 3 -> ValidationResult.Error("El nombre debe tener al menos 3 caracteres")
            name.length > 50 -> ValidationResult.Error("El nombre no puede exceder 50 caracteres")
            else -> ValidationResult.Success
        }
    }

    fun validateGoal(goal: String): ValidationResult {
        return when {
            goal.isBlank() -> ValidationResult.Error("El objetivo es obligatorio")
            goal.length < 5 -> ValidationResult.Error("Describe mejor tu objetivo")
            else -> ValidationResult.Success
        }
    }

    fun validateSessionsPerWeek(sessions: String): ValidationResult {
        return try {
            val sessionsNum = sessions.toInt()
            when {
                sessionsNum < 1 -> ValidationResult.Error("Mínimo 1 sesión por semana")
                sessionsNum > 7 -> ValidationResult.Error("Máximo 7 sesiones por semana")
                else -> ValidationResult.Success
            }
        } catch (e: Exception) {
            ValidationResult.Error("Debe ser un número (ej: 3)")
        }
    }

    fun validateTrainingDays(days: List<String>): ValidationResult {
        return if (days.isEmpty()) {
            ValidationResult.Error("Selecciona al menos un día")
        } else {
            ValidationResult.Success
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}