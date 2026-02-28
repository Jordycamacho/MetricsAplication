package com.fitapp.appfit.repository

import com.fitapp.appfit.response.routine.request.*
import com.fitapp.appfit.response.routine.response.*
import com.fitapp.appfit.response.page.PageResponse
import com.fitapp.appfit.service.RoutineService
import com.fitapp.appfit.utils.Resource
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException

class RoutineRepository {
    private val service = RoutineService.instance

    // ── CRUD básico ──────────────────────────────────────────────────────────

    suspend fun createRoutine(request: CreateRoutineRequest) =
        call { service.createRoutine(request) }

    suspend fun getRoutine(id: Long) =
        call { service.getRoutine(id) }

    suspend fun getRoutineForTraining(id: Long) =
        call { service.getRoutineForTraining(id) }

    suspend fun updateRoutine(id: Long, request: UpdateRoutineRequest) =
        call { service.updateRoutine(id, request) }

    suspend fun deleteRoutine(id: Long) =
        callUnit { service.deleteRoutine(id) }

    // ── Listados ─────────────────────────────────────────────────────────────

    suspend fun getRoutines(page: Int = 0, size: Int = 20) =
        call { service.getRoutines(page, size) }

    suspend fun getRoutinesWithFilters(
        sportId: Long? = null,
        name: String? = null,
        isActive: Boolean? = null,
        sortBy: String = "createdAt",
        sortDirection: String = "DESC",
        page: Int = 0,
        size: Int = 20
    ) = call { service.getRoutinesWithFilters(sportId, name, isActive, sortBy, sortDirection, page, size) }

    suspend fun getRecentRoutines(limit: Int = 5) =
        call { service.getRecentRoutines(limit) }

    suspend fun getLastUsedRoutines(limit: Int = 3) =
        call { service.getLastUsedRoutines(limit) }

    suspend fun getActiveRoutines() =
        call { service.getActiveRoutines() }

    // ── Estado y estadísticas ────────────────────────────────────────────────

    suspend fun toggleRoutineActiveStatus(id: Long, active: Boolean) =
        callUnit { service.toggleRoutineActiveStatus(id, active) }

    suspend fun markRoutineAsUsed(id: Long) =
        callUnit { service.markRoutineAsUsed(id) }

    suspend fun getRoutineStatistics() =
        call { service.getRoutineStatistics() }

    // ── Funciones genéricas de manejo de respuesta ───────────────────────────

    private suspend fun <T> call(block: suspend () -> Response<T>): Resource<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error("El servidor respondió sin datos")
            } else {
                Resource.Error(httpErrorMessage(response.code(), response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Resource.Error(exceptionMessage(e))
        }
    }

    private suspend fun callUnit(block: suspend () -> Response<Unit>): Resource<Unit> {
        return try {
            val response = block()
            if (response.isSuccessful) Resource.Success(Unit)
            else Resource.Error(httpErrorMessage(response.code(), response.errorBody()?.string()))
        } catch (e: Exception) {
            Resource.Error(exceptionMessage(e))
        }
    }

    private fun httpErrorMessage(code: Int, body: String?): String = when (code) {
        401 -> "Sesión expirada. Vuelve a iniciar sesión."
        403 -> "No tienes permisos para realizar esta acción."
        404 -> "Recurso no encontrado."
        500 -> "Error del servidor. Intenta nuevamente."
        else -> "Error $code: ${body ?: "Error desconocido"}"
    }

    private fun exceptionMessage(e: Exception): String = when (e) {
        is SocketTimeoutException -> "Tiempo de espera agotado. Verifica tu conexión."
        is ConnectException -> "Sin conexión. Verifica tu internet."
        is retrofit2.HttpException -> httpErrorMessage(e.code(), e.message())
        else -> "Error: ${e.message ?: "Error desconocido"}"
    }
}