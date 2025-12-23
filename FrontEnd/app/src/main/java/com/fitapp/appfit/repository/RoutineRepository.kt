package com.fitapp.appfit.repository

import android.util.Log
import com.fitapp.appfit.response.routine.request.*
import com.fitapp.appfit.response.routine.response.*
import com.fitapp.appfit.response.page.PageResponse
import com.fitapp.appfit.service.RoutineService
import com.fitapp.appfit.utils.Resource
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException

class RoutineRepository {
    private val routineService = RoutineService.instance

    companion object {
        private const val TAG = "RoutineRepository"
    }

    // ==================== CRUD básico ====================

    suspend fun createRoutine(request: CreateRoutineRequest): Resource<RoutineResponse> {
        return try {
            Log.d(TAG, "Enviando solicitud de creación de rutina:")
            val response = routineService.createRoutine(request)
            handleResponse(response, "crear rutina")
        } catch (e: Exception) {
            handleException(e, "crear rutina")
        }
    }

    suspend fun getRoutine(id: Long): Resource<RoutineResponse> {
        return try {
            Log.d(TAG, "Obteniendo rutina con ID: $id")
            val response = routineService.getRoutine(id)
            handleResponse(response, "obtener rutina por ID")
        } catch (e: Exception) {
            handleException(e, "obtener rutina por ID")
        }
    }

    suspend fun updateRoutine(id: Long, request: UpdateRoutineRequest): Resource<RoutineResponse> {
        return try {
            Log.d(TAG, "Actualizando rutina con ID: $id")
            val response = routineService.updateRoutine(id, request)
            handleResponse(response, "actualizar rutina")
        } catch (e: Exception) {
            handleException(e, "actualizar rutina")
        }
    }

    suspend fun deleteRoutine(id: Long): Resource<Unit> {
        return try {
            Log.d(TAG, "Eliminando rutina con ID: $id")
            val response = routineService.deleteRoutine(id)
            handleResponseUnit(response, "eliminar rutina")
        } catch (e: Exception) {
            handleExceptionUnit(e, "eliminar rutina")
        }
    }

    // ==================== Gestión de ejercicios ====================

    suspend fun addExercisesToRoutine(request: AddExercisesToRoutineRequest): Resource<RoutineResponse> {
        return try {
            Log.d(TAG, "Agregando ejercicios a rutina")
            val response = routineService.addExercisesToRoutine(request)
            handleResponse(response, "agregar ejercicios a rutina")
        } catch (e: Exception) {
            handleException(e, "agregar ejercicios a rutina")
        }
    }

    // ==================== Listados ====================

    suspend fun getRoutines(
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "createdAt",
        sortDirection: String = "DESC"
    ): Resource<PageResponse<RoutineSummaryResponse>> {
        return try {
            Log.d(TAG, "Obteniendo lista de rutinas (página $page, tamaño $size)")
            val response = routineService.getRoutines(page, size, sortBy, sortDirection)
            handleResponse(response, "obtener rutinas paginadas")
        } catch (e: Exception) {
            handleException(e, "obtener rutinas paginadas")
        }
    }

    suspend fun getRecentRoutines(limit: Int = 5): Resource<List<RoutineSummaryResponse>> {
        return try {
            Log.d(TAG, "Obteniendo rutinas recientes (límite: $limit)")
            val response = routineService.getRecentRoutines(limit)
            handleResponse(response, "obtener rutinas recientes")
        } catch (e: Exception) {
            handleException(e, "obtener rutinas recientes")
        }
    }

    suspend fun getActiveRoutines(): Resource<List<RoutineSummaryResponse>> {
        return try {
            Log.d(TAG, "Obteniendo rutinas activas")
            val response = routineService.getActiveRoutines()
            handleResponse(response, "obtener rutinas activas")
        } catch (e: Exception) {
            handleException(e, "obtener rutinas activas")
        }
    }

    // ==================== Filtros ====================

    suspend fun getRoutinesWithFilters(
        sportId: Long? = null,
        name: String? = null,
        isActive: Boolean? = null,
        sortBy: String = "createdAt",
        sortDirection: String = "DESC",
        page: Int = 0,
        size: Int = 10
    ): Resource<PageResponse<RoutineSummaryResponse>> {
        return try {
            Log.d(TAG, "Obteniendo rutinas con filtros")
            val response = routineService.getRoutinesWithFilters(
                sportId, name, isActive, sortBy, sortDirection, page, size
            )
            handleResponse(response, "obtener rutinas con filtros")
        } catch (e: Exception) {
            handleException(e, "obtener rutinas con filtros")
        }
    }

    // ==================== Estado activo ====================

    suspend fun toggleRoutineActiveStatus(id: Long, active: Boolean): Resource<Unit> {
        return try {
            Log.d(TAG, "Cambiando estado de rutina (ID: $id) a activo=$active")
            val response = routineService.toggleRoutineActiveStatus(id, active)
            handleResponseUnit(response, "cambiar estado de rutina")
        } catch (e: Exception) {
            handleExceptionUnit(e, "cambiar estado de rutina")
        }
    }

    // ==================== Estadísticas ====================

    suspend fun getRoutineStatistics(): Resource<RoutineStatisticsResponse> {
        return try {
            Log.d(TAG, "Obteniendo estadísticas de rutinas")
            val response = routineService.getRoutineStatistics()
            handleResponse(response, "obtener estadísticas de rutinas")
        } catch (e: Exception) {
            handleException(e, "obtener estadísticas de rutinas")
        }
    }

    // ==================== Funciones auxiliares ====================

    private fun <T> handleResponse(response: Response<T>, operation: String): Resource<T> {
        Log.d(TAG, "Respuesta recibida - Código: ${response.code()}")

        return if (response.isSuccessful) {
            response.body()?.let { body ->
                Log.d(TAG, "✅ Operación '$operation' exitosa")
                Resource.Success(body)
            } ?: run {
                Log.w(TAG, "⚠️ Respuesta vacía del servidor en operación: $operation")
                Resource.Error("El servidor respondió sin datos")
            }
        } else {
            val errorMsg = "Error ${response.code()}: ${response.errorBody()?.string() ?: response.message()}"
            Log.e(TAG, "❌ Error en operación '$operation': $errorMsg")

            // Manejo específico de errores
            when (response.code()) {
                500 -> Resource.Error("Error del servidor (500). Intenta nuevamente.")
                401 -> Resource.Error("Sesión expirada. Vuelve a iniciar sesión.")
                404 -> Resource.Error("Recurso no encontrado.")
                403 -> Resource.Error("No tienes permisos para realizar esta acción.")
                else -> Resource.Error(errorMsg)
            }
        }
    }

    private fun <T> handleException(e: Exception, operation: String): Resource<T> {
        Log.e(TAG, "❌ Excepción en operación '$operation': ${e.message}", e)

        // Verificar tipo de excepción
        return when (e) {
            is SocketTimeoutException -> Resource.Error("Tiempo de espera agotado. Verifica tu conexión.")
            is ConnectException -> Resource.Error("Error de conexión. Verifica tu internet.")
            is retrofit2.HttpException -> {
                when (e.code()) {
                    500 -> Resource.Error("Error del servidor (500). Intenta nuevamente.")
                    401 -> Resource.Error("Sesión expirada. Vuelve a iniciar sesión.")
                    else -> Resource.Error("Error ${e.code()}: ${e.message()}")
                }
            }
            else -> Resource.Error("Error: ${e.message ?: "Error desconocido"}")
        }
    }

    private fun handleResponseUnit(response: Response<Unit>, operation: String): Resource<Unit> {
        Log.d(TAG, "Respuesta recibida - Código: ${response.code()}")

        return if (response.isSuccessful) {
            Log.d(TAG, "✅ Operación '$operation' exitosa")
            Resource.Success(Unit)
        } else {
            val errorMsg = "Error ${response.code()}: ${response.errorBody()?.string() ?: response.message()}"
            Log.e(TAG, "❌ Error en operación '$operation': $errorMsg")

            when (response.code()) {
                500 -> Resource.Error("Error del servidor (500). Intenta nuevamente.")
                401 -> Resource.Error("Sesión expirada. Vuelve a iniciar sesión.")
                else -> Resource.Error(errorMsg)
            }
        }
    }

    private fun handleExceptionUnit(e: Exception, operation: String): Resource<Unit> {
        Log.e(TAG, "❌ Excepción en operación '$operation': ${e.message}", e)

        return when (e) {
            is SocketTimeoutException -> Resource.Error("Tiempo de espera agotado. Verifica tu conexión.")
            is ConnectException -> Resource.Error("Error de conexión. Verifica tu internet.")
            is retrofit2.HttpException -> {
                when (e.code()) {
                    500 -> Resource.Error("Error del servidor (500). Intenta nuevamente.")
                    401 -> Resource.Error("Sesión expirada. Vuelve a iniciar sesión.")
                    else -> Resource.Error("Error ${e.code()}: ${e.message()}")
                }
            }
            else -> Resource.Error("Error: ${e.message ?: "Error desconocido"}")
        }
    }
}