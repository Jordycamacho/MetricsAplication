package com.fitapp.appfit.feature.routine.data

import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.routine.model.rutinexercise.request.AddExerciseToRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import com.fitapp.appfit.feature.routine.data.RoutineExerciseService
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException

class RoutineExerciseRepository {

    private val service = RoutineExerciseService.Companion.instance

    suspend fun getRoutineExercises(routineId: Long): Resource<List<RoutineExerciseResponse>> =
        call { service.getRoutineExercises(routineId) }

    suspend fun addExerciseToRoutine(
        routineId: Long,
        request: AddExerciseToRoutineRequest
    ): Resource<RoutineExerciseResponse> =
        call { service.addExerciseToRoutine(routineId, request) }

    suspend fun updateExerciseInRoutine(
        routineId: Long,
        exerciseId: Long,
        request: AddExerciseToRoutineRequest
    ): Resource<RoutineExerciseResponse> =
        call { service.updateExerciseInRoutine(routineId, exerciseId, request) }

    suspend fun removeExerciseFromRoutine(routineId: Long, exerciseId: Long): Resource<Unit> =
        callUnit { service.removeExerciseFromRoutine(routineId, exerciseId) }

    suspend fun reorderExercises(routineId: Long, exerciseIds: List<Long>): Resource<Unit> =
        callUnit { service.reorderExercises(routineId, exerciseIds) }

    // ── Helpers genéricos ────────────────────────────────────────────────────

    private suspend fun <T> call(block: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("Respuesta vacía del servidor")
            } else {
                Resource.Error(httpError(response.code()))
            }
        } catch (e: Exception) {
            Resource.Error(networkError(e))
        }
    }

    private suspend fun callUnit(block: suspend () -> Response<*>): Resource<Unit> {
        return try {
            val response = block()
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(httpError(response.code()))
        } catch (e: Exception) {
            Resource.Error(networkError(e))
        }
    }

    private fun httpError(code: Int) = when (code) {
        401 -> "Sesión expirada, vuelve a iniciar sesión"
        403 -> "No tienes permisos para esta acción"
        404 -> "Recurso no encontrado"
        500 -> "Error interno del servidor"
        else -> "Error $code"
    }

    private fun networkError(e: Exception) = when (e) {
        is SocketTimeoutException -> "Tiempo de espera agotado"
        is ConnectException -> "Sin conexión al servidor"
        else -> e.message ?: "Error de red"
    }
}