package com.fitapp.appfit.feature.routine.data

import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.routine.model.rutinexercise.request.AddExerciseToRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineExerciseResponse
import com.fitapp.appfit.feature.routine.util.RoutineErrorHandler
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
        routineExerciseId: Long,
        request: AddExerciseToRoutineRequest
    ): Resource<RoutineExerciseResponse> =
        call { service.updateExerciseInRoutine(routineId, routineExerciseId, request) }

    suspend fun removeExerciseFromRoutine(routineId: Long, routineExerciseId: Long): Resource<Unit> =
        callUnitNoContent { service.removeExerciseFromRoutine(routineId, routineExerciseId) }

    suspend fun reorderExercises(routineId: Long, exerciseIds: List<Long>): Resource<Unit> =
        callUnit { service.reorderExercises(routineId, exerciseIds) }

    private suspend fun <T> call(block: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("Respuesta vacía del servidor")
            } else {
                Resource.Error(RoutineErrorHandler.getErrorMessage(response))
            }
        } catch (e: Exception) {
            Resource.Error(networkError(e))
        }
    }

    private suspend fun callUnit(block: suspend () -> Response<*>): Resource<Unit> {
        return try {
            val response = block()
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(RoutineErrorHandler.getErrorMessage(response))
        } catch (e: Exception) {
            Resource.Error(networkError(e))
        }
    }

    private suspend fun callUnitNoContent(block: suspend () -> Response<*>): Resource<Unit> {
        return try {
            val response = block()
            if (response.code() == 204) Resource.Success(Unit)
            else Resource.Error(RoutineErrorHandler.getErrorMessage(response))
        } catch (e: Exception) {
            Resource.Error(networkError(e))
        }
    }

    private fun networkError(e: Exception) = when (e) {
        is SocketTimeoutException -> "Tiempo de espera agotado"
        is ConnectException -> "Sin conexión al servidor"
        else -> e.message ?: "Error de red"
    }
}
