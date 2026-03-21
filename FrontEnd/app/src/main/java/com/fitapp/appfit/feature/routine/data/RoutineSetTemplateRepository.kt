package com.fitapp.appfit.feature.routine.data

import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.routine.model.rutinexercise.response.RoutineSetTemplateResponse
import com.fitapp.appfit.feature.routine.model.setemplate.request.CreateSetTemplateRequest
import com.fitapp.appfit.feature.routine.model.setemplate.request.UpdateSetTemplateRequest
import com.fitapp.appfit.feature.routine.data.RoutineSetTemplateService
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException

class RoutineSetTemplateRepository {

    private val service = RoutineSetTemplateService.Companion.instance

    suspend fun createSetTemplate(request: CreateSetTemplateRequest): Resource<RoutineSetTemplateResponse> =
        call { service.createSetTemplate(request) }

    suspend fun updateSetTemplate(id: Long, request: UpdateSetTemplateRequest): Resource<RoutineSetTemplateResponse> =
        call { service.updateSetTemplate(id, request) }

    suspend fun getSetTemplate(id: Long): Resource<RoutineSetTemplateResponse> =
        call { service.getSetTemplate(id) }

    suspend fun getSetTemplatesByRoutineExercise(routineExerciseId: Long): Resource<List<RoutineSetTemplateResponse>> =
        call { service.getSetTemplatesByRoutineExercise(routineExerciseId) }

    suspend fun deleteSetTemplate(id: Long): Resource<Unit> =
        callUnit { service.deleteSetTemplate(id) }

    suspend fun deleteSetTemplatesByRoutineExercise(routineExerciseId: Long): Resource<Unit> =
        callUnit { service.deleteSetTemplatesByRoutineExercise(routineExerciseId) }

    suspend fun reorderSetTemplates(routineExerciseId: Long, ids: List<Long>): Resource<RoutineSetTemplateResponse> =
        call { service.reorderSetTemplates(routineExerciseId, ids) }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private suspend fun <T> call(block: suspend () -> Response<T>): Resource<T> {
        return try {
            val r = block()
            if (r.isSuccessful) {
                r.body()?.let { Resource.Success(it) } ?: Resource.Error("Respuesta vacía del servidor")
            } else {
                Resource.Error(httpError(r.code()))
            }
        } catch (e: Exception) {
            Resource.Error(networkError(e))
        }
    }

    private suspend fun callUnit(block: suspend () -> Response<*>): Resource<Unit> {
        return try {
            val r = block()
            if (r.isSuccessful) Resource.Success(Unit)
            else Resource.Error(httpError(r.code()))
        } catch (e: Exception) {
            Resource.Error(networkError(e))
        }
    }

    private fun httpError(code: Int) = when (code) {
        401 -> "Sesión expirada"
        403 -> "Sin permisos"
        404 -> "No encontrado"
        500 -> "Error del servidor"
        else -> "Error $code"
    }

    private fun networkError(e: Exception) = when (e) {
        is SocketTimeoutException -> "Tiempo de espera agotado"
        is ConnectException -> "Sin conexión al servidor"
        else -> e.message ?: "Error de red"
    }
}