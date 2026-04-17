package com.fitapp.appfit.feature.parameter.util

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.Response

/**
 * Estructura de error del backend
 */
data class ApiError(
    @SerializedName("timestamp") val timestamp: String?,
    @SerializedName("status") val status: Int?,
    @SerializedName("error") val error: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("path") val path: String?,
    @SerializedName("validationErrors") val validationErrors: Map<String, String>?
)

/**
 * Clase para procesar errores del API de parámetros
 */
object ParameterErrorHandler {

    /**
     * Extrae un mensaje legible de un error de Retrofit
     */
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

    /**
     * Parsea un ApiError y retorna mensaje legible
     */
    private fun parseApiError(apiError: ApiError, code: Int): String {
        // Si hay errores de validación, mostrarlos
        if (!apiError.validationErrors.isNullOrEmpty()) {
            val errors = apiError.validationErrors.values.joinToString("\n• ")
            return "Errores de validación:\n• $errors"
        }

        // Usar el mensaje del backend
        val message = apiError.message ?: apiError.error

        return when (code) {
            400 -> message ?: "Los datos proporcionados no son válidos"
            401 -> "Tu sesión ha expirado. Por favor, inicia sesión nuevamente"
            403 -> message ?: "No tienes permisos para realizar esta acción"
            404 -> message ?: "El parámetro solicitado no existe"
            409 -> message ?: "Ya existe un parámetro con este nombre"
            429 -> message ?: "Has alcanzado el límite de parámetros de tu plan"
            500 -> "Error del servidor. Por favor, intenta más tarde"
            else -> message ?: "Error desconocido (código $code)"
        }
    }

    /**
     * Mensaje por defecto según código HTTP
     */
    private fun getDefaultErrorMessage(code: Int): String {
        return when (code) {
            400 -> "Los datos proporcionados no son válidos"
            401 -> "Tu sesión ha expirado. Por favor, inicia sesión nuevamente"
            403 -> "No tienes permisos para realizar esta acción"
            404 -> "El parámetro solicitado no existe"
            409 -> "Ya existe un parámetro con este nombre"
            429 -> "Has alcanzado el límite de parámetros personalizados de tu plan.\n\n" +
                    "Actualiza tu suscripción para crear más parámetros."
            500 -> "Error del servidor. Por favor, intenta más tarde"
            503 -> "El servicio no está disponible. Por favor, intenta más tarde"
            else -> "Error de conexión (código $code)"
        }
    }

    /**
     * Mensaje para excepciones de red
     */
    fun getNetworkErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("Unable to resolve host") == true ->
                "Sin conexión a internet. Verifica tu red."
            exception.message?.contains("timeout") == true ->
                "La conexión tardó demasiado. Intenta nuevamente."
            exception.message?.contains("SSL") == true ->
                "Error de seguridad en la conexión."
            else -> exception.message ?: "Error de conexión desconocido"
        }
    }

    /**
     * Mensaje específico para error de límite de suscripción
     */
    fun getSubscriptionLimitMessage(currentPlan: String = "FREE"): String {
        return when (currentPlan.uppercase()) {
            "FREE" -> """
                Has alcanzado el límite de parámetros personalizados de tu plan FREE.
                
                Actualiza a PRO para:
                • Hasta 50 parámetros personalizados
                • Métricas avanzadas
                • Y mucho más
                
                ¿Deseas actualizar tu plan?
            """.trimIndent()

            "PRO" -> """
                Has alcanzado el límite de 50 parámetros personalizados del plan PRO.
                
                Contacta con soporte si necesitas más parámetros.
            """.trimIndent()

            else -> "Has alcanzado el límite de parámetros de tu plan.\nActualiza tu suscripción para crear más."
        }
    }
}