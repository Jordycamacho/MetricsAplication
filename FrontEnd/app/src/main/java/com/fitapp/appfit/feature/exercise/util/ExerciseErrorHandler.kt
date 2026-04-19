package com.fitapp.appfit.feature.exercise.util

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.Response

data class ApiError(
    @SerializedName("timestamp") val timestamp: String?,
    @SerializedName("status") val status: Int?,
    @SerializedName("error") val error: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("path") val path: String?,
    @SerializedName("validationErrors") val validationErrors: Map<String, String>?
)

object ExerciseErrorHandler {

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
                "• ${field.capitalize()}: $msg"
            }
            return "Errores de validación:\n$errors"
        }

        val message = apiError.message ?: apiError.error

        return when (code) {
            400 -> message ?: "Los datos del ejercicio no son válidos"
            401 -> "Tu sesión ha expirado. Inicia sesión nuevamente"
            403 -> message ?: "No tienes permisos para realizar esta acción"
            404 -> message ?: "El ejercicio solicitado no existe"
            409 -> message ?: "Ya existe un ejercicio con este nombre"
            429 -> getSubscriptionLimitMessage(message)   // ← llama a la privada
            500 -> "Error del servidor. Intenta más tarde"
            503 -> "El servicio no está disponible temporalmente"
            else -> message ?: "Error desconocido (código $code)"
        }
    }

    private fun getDefaultErrorMessage(code: Int): String {
        return when (code) {
            400 -> "Los datos del ejercicio no son válidos"
            401 -> "Tu sesión ha expirado. Por favor, inicia sesión nuevamente"
            403 -> "No tienes permisos para realizar esta acción"
            404 -> "El ejercicio solicitado no existe o fue eliminado"
            409 -> "Ya existe un ejercicio con este nombre"
            429 -> "Has alcanzado el límite de ejercicios personalizados.\nActualiza tu plan para crear más."
            500 -> "Error del servidor. Por favor, intenta más tarde"
            503 -> "El servicio no está disponible. Intenta más tarde"
            else -> "Error de conexión (código $code)"
        }
    }

    // ✅ Renombrada para evitar conflicto
    fun getSubscriptionLimitMessageForPlan(currentPlan: String = "FREE"): String {
        return when (currentPlan.uppercase()) {
            "FREE" -> """
                Plan FREE: Límite alcanzado
                
                Has creado el máximo de ejercicios personalizados permitidos en tu plan gratuito.
                
                Actualiza a PRO para:
                • Hasta 200 ejercicios personalizados
                • Biblioteca de ejercicios premium
                • Compartir ejercicios públicamente
                • Y mucho más
                
                ¿Deseas actualizar tu plan?
            """.trimIndent()

            "PRO" -> """
                Plan PRO: Límite alcanzado
                
                Has alcanzado el límite de 200 ejercicios personalizados.
                
                Si necesitas más ejercicios, contacta con soporte para opciones empresariales.
            """.trimIndent()

            else -> "Has alcanzado el límite de ejercicios de tu plan.\nActualiza tu suscripción."
        }
    }

    // Esta se queda igual (privada)
    private fun getSubscriptionLimitMessage(backendMessage: String?): String {
        return backendMessage ?: """
            Has alcanzado el límite de ejercicios personalizados de tu plan.
            
            Actualiza a un plan superior para:
            • Crear ejercicios ilimitados
            • Acceso a ejercicios premium
            • Compartir tus ejercicios
            
            ¿Deseas actualizar tu plan?
        """.trimIndent()
    }

    fun getNetworkErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("Unable to resolve host") == true ->
                "Sin conexión a internet. Verifica tu red."
            exception.message?.contains("timeout") == true ->
                "La conexión tardó demasiado. Intenta nuevamente."
            exception.message?.contains("SSL") == true ->
                "Error de seguridad en la conexión."
            exception.message?.contains("SocketTimeout") == true ->
                "El servidor no responde. Intenta más tarde."
            else -> exception.message ?: "Error de conexión desconocido"
        }
    }

    fun getCannotEditPublicMessage(): String {
        return """
            No puedes editar un ejercicio público del sistema.
            
            Puedes:
            • Duplicar el ejercicio para crear tu versión
            • Crear un ejercicio nuevo similar
        """.trimIndent()
    }

    fun getExerciseInUseMessage(usageCount: Int): String {
        return """
            Este ejercicio está siendo usado en $usageCount rutina${if (usageCount > 1) "s" else ""}.
            
            ¿Estás seguro de que deseas eliminarlo?
            Esta acción puede afectar tus rutinas activas.
        """.trimIndent()
    }

    fun getMakePublicConfirmationMessage(): String {
        return """
            ¿Hacer este ejercicio público?
            
            Al hacerlo público:
            • Otros usuarios podrán verlo y usarlo
            • No podrás editarlo después
            • Aparecerá en búsquedas públicas
            
            Esta acción es permanente.
        """.trimIndent()
    }
}