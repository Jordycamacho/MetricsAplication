package com.fitapp.appfit.repository

import android.util.Log
import com.fitapp.appfit.response.routine.CreateRoutineRequest
import com.fitapp.appfit.response.routine.RoutineResponse
import com.fitapp.appfit.service.RoutineService
import com.fitapp.appfit.utils.Resource

class RoutineRepository {
    private val routineService = RoutineService.instance

    // TAG para logs
    companion object {
        private const val TAG = "RoutineRepository"
    }

    suspend fun createRoutine(request: CreateRoutineRequest): Resource<RoutineResponse> {
        return try {
            Log.d(TAG, "Enviando solicitud de creación de rutina:")
            Log.d(TAG, "Request: $request")

            val response = routineService.createRoutine(request)

            Log.d(TAG, "Respuesta recibida - Código: ${response.code()}")
            Log.d(TAG, "Respuesta: ${response.body()}")

            if (response.isSuccessful) {
                response.body()?.let {
                    Log.d(TAG, "✅ Rutina creada exitosamente")
                    Resource.Success(it)
                } ?: run {
                    Log.w(TAG, "⚠️ Respuesta vacía del servidor")
                    Resource.Error("El servidor respondió sin datos")
                }
            } else {
                val errorMsg = "Error ${response.code()}: ${response.errorBody()?.string() ?: response.message()}"
                Log.e(TAG, errorMsg)
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error de red/excepción: ${e.message}", e)
            Resource.Error("Error de conexión: ${e.message ?: "Verifica tu internet"}")
        }
    }
}