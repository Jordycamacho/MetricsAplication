package com.fitapp.appfit.feature.routine.util

import com.fitapp.appfit.feature.exercise.util.ApiError
import com.google.gson.Gson
import retrofit2.Response

object RoutineErrorHandler {

    fun <T> getErrorMessage(response: Response<T>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val apiError = Gson().fromJson(errorBody, ApiError::class.java)
                parseApiError(apiError, response.code())
            } else {
                getDefaultErrorMessage(response.code())
            }
        } catch (e: Exception) {
            getDefaultErrorMessage(response.code())
        }
    }

    private fun parseApiError(apiError: ApiError, code: Int): String {
        if (!apiError.validationErrors.isNullOrEmpty()) {
            val errors = apiError.validationErrors.entries.joinToString("\n") { (field, msg) ->
                "• ${formatFieldName(field)}: $msg"
            }
            return "Errores de validación:\n$errors"
        }

        val message = apiError.message ?: apiError.error

        return when (code) {
            400 -> message ?: "Los datos de la rutina no son válidos"
            401 -> "Tu sesión ha expirado. Inicia sesión nuevamente"
            403 -> message ?: getSubscriptionLimitMessage(message)
            404 -> message ?: "La rutina solicitada no existe"
            409 -> message ?: "Ya existe una rutina con este nombre"
            429 -> getSubscriptionLimitMessage(message)
            500 -> "Error del servidor. Intenta más tarde"
            503 -> "El servicio no está disponible temporalmente"
            else -> message ?: "Error desconocido (código $code)"
        }
    }

    private fun getDefaultErrorMessage(code: Int): String {
        return when (code) {
            400 -> "Datos de la rutina inválidos"
            401 -> "Tu sesión ha expirado"
            403 -> "No tienes permisos para realizar esta acción"
            404 -> "Rutina no encontrada"
            409 -> "Ya existe una rutina con este nombre"
            429 -> "Has alcanzado el límite de tu plan"
            500 -> "Error del servidor"
            503 -> "Servicio no disponible"
            else -> "Error de conexión (código $code)"
        }
    }

    private fun getSubscriptionLimitMessage(message: String?): String {
        return when {
            message?.contains("rutinas", ignoreCase = true) == true ||
                    message?.contains("routines", ignoreCase = true) == true ->
                "Límite de rutinas alcanzado\n\n" +
                        "Has llegado al máximo de rutinas permitidas en tu plan. " +
                        "Actualiza tu suscripción para crear más rutinas."

            message?.contains("ejercicios", ignoreCase = true) == true ||
                    message?.contains("exercises", ignoreCase = true) == true ->
                "Límite de ejercicios alcanzado\n\n" +
                        "Has llegado al máximo de ejercicios por rutina permitidos en tu plan. " +
                        "Actualiza tu suscripción para añadir más ejercicios."

            message?.contains("sets", ignoreCase = true) == true ||
                    message?.contains("series", ignoreCase = true) == true ->
                "Límite de sets alcanzado\n\n" +
                        "Has llegado al máximo de sets por ejercicio permitidos en tu plan. " +
                        "Actualiza tu suscripción para añadir más sets."

            else -> message ?: "Has alcanzado el límite de tu plan. Actualiza tu suscripción."
        }
    }

    private fun formatFieldName(field: String): String {
        return when (field.lowercase()) {
            "name" -> "Nombre"
            "description" -> "Descripción"
            "goal" -> "Objetivo"
            "sessionsperweek" -> "Sesiones por semana"
            "trainingdays" -> "Días de entrenamiento"
            "sportid" -> "Deporte"
            "version" -> "Versión"
            else -> field.replaceFirstChar { it.uppercase() }
        }
    }

    /**
     * Mensajes específicos para operaciones de rutinas
     */
    fun getOperationErrorMessage(operation: String, error: Throwable): String {
        return when (operation) {
            "CREATE" -> "No se pudo crear la rutina: ${error.message}"
            "UPDATE" -> "No se pudo actualizar la rutina: ${error.message}"
            "DELETE" -> "No se pudo eliminar la rutina: ${error.message}"
            "IMPORT" -> "No se pudo importar la rutina: ${error.message}"
            "EXPORT" -> "No se pudo compartir la rutina: ${error.message}"
            else -> "Error: ${error.message}"
        }
    }
}