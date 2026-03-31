package com.fitapp.appfit.feature.marketplace.data

import android.content.Context
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.marketplace.model.request.AddPackageItemRequest
import com.fitapp.appfit.feature.marketplace.model.request.CreatePackageRequest
import com.fitapp.appfit.feature.marketplace.model.request.UpdatePackageRequest
import com.fitapp.appfit.feature.marketplace.model.response.PackageResponse
import com.fitapp.appfit.feature.marketplace.model.response.PackageSummaryResponse
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException

class PackageRepository(private val context: Context) {

    private val service = PackageService.instance

    // ── CRUD ───────────────────────────────────────────────────────────────────

    suspend fun createPackage(request: CreatePackageRequest): Resource<PackageResponse> =
        call { service.createPackage(request) }

    suspend fun getPackageById(id: Long): Resource<PackageResponse> =
        call { service.getPackageById(id) }

    suspend fun getPackageBySlug(slug: String): Resource<PackageResponse> =
        call { service.getPackageBySlug(slug) }

    suspend fun updatePackage(id: Long, request: UpdatePackageRequest): Resource<PackageResponse> =
        call { service.updatePackage(id, request) }

    suspend fun deletePackage(id: Long): Resource<Unit> =
        callUnit { service.deletePackage(id) }

    // ── Búsqueda y filtros ────────────────────────────────────────────────────

    suspend fun searchMarketplace(
        search: String? = null,
        packageType: String? = null,
        isFree: Boolean? = null,
        minRating: Double? = null,
        requiresSubscription: String? = null,
        sortBy: String = "createdAt",
        sortDirection: String = "DESC",
        page: Int = 0,
        size: Int = 20
    ): Resource<com.fitapp.appfit.shared.model.PageResponse<PackageSummaryResponse>> =
        call {
            service.searchMarketplace(
                search, packageType, isFree, minRating, requiresSubscription,
                sortBy, sortDirection, page, size
            )
        }

    suspend fun getOfficialPackages(
        page: Int = 0,
        size: Int = 20
    ): Resource<com.fitapp.appfit.shared.model.PageResponse<PackageSummaryResponse>> =
        call { service.getOfficialPackages(page, size) }

    suspend fun getUserPackages(
        userId: Long,
        page: Int = 0,
        size: Int = 20
    ): Resource<com.fitapp.appfit.shared.model.PageResponse<PackageSummaryResponse>> =
        call { service.getUserPackages(userId, page, size) }

    suspend fun getUserPurchasedPackages(
        page: Int = 0,
        size: Int = 20
    ): Resource<com.fitapp.appfit.shared.model.PageResponse<PackageSummaryResponse>> =
        call { service.getUserPurchasedPackages(page, size) }

    // ── Items ──────────────────────────────────────────────────────────────────

    suspend fun addItemToPackage(
        packageId: Long,
        request: AddPackageItemRequest
    ): Resource<com.fitapp.appfit.feature.marketplace.model.response.PackageItemResponse> =
        call { service.addItemToPackage(packageId, request) }

    suspend fun removeItemFromPackage(
        packageId: Long,
        itemId: Long
    ): Resource<Unit> =
        callUnit { service.removeItemFromPackage(packageId, itemId) }

    suspend fun reorderPackageItems(
        packageId: Long,
        itemIds: List<Long>
    ): Resource<PackageResponse> =
        call { service.reorderPackageItems(packageId, itemIds) }

    // ── Estado ─────────────────────────────────────────────────────────────────

    suspend fun publishPackage(id: Long): Resource<Unit> =
        callUnit { service.publishPackage(id) }

    suspend fun deprecatePackage(id: Long, reason: String? = null): Resource<Unit> =
        callUnit { service.deprecatePackage(id, reason) }

    suspend fun suspendPackage(id: Long, reason: String? = null): Resource<Unit> =
        callUnit { service.suspendPackage(id, reason) }

    suspend fun unsuspendPackage(id: Long): Resource<Unit> =
        callUnit { service.unsuspendPackage(id) }

    // ── Especiales ─────────────────────────────────────────────────────────────

    suspend fun getTrendingPackages(
        limit: Int = 10
    ): Resource<List<PackageSummaryResponse>> =
        call { service.getTrendingPackages(limit) }

    suspend fun getTopRatedPackages(
        limit: Int = 10
    ): Resource<List<PackageSummaryResponse>> =
        call { service.getTopRatedPackages(limit) }

    suspend fun getPackageStatistics(
        id: Long
    ): Resource<PackageStatisticsResponse> =
        call { service.getPackageStatistics(id) }

    // ── Interacciones ──────────────────────────────────────────────────────────

    suspend fun downloadPackage(id: Long): Resource<Unit> =
        callUnit { service.downloadPackage(id) }

    suspend fun ratePackage(id: Long, rating: Double): Resource<Unit> =
        callUnit { service.ratePackage(id, rating) }

    suspend fun purchasePackage(id: Long): Resource<Unit> =
        callUnit { service.purchasePackage(id) }

    // ── Funciones genéricas ───────────────────────────────────────────────────

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
            android.util.Log.e("REPO_ERROR", "Exception en call()", e)
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
        403 -> "No tienes permisos para acceder a este paquete."
        404 -> "Paquete no encontrado."
        500 -> "Error del servidor. Intenta nuevamente."
        else -> "Error $code: ${body ?: "Error desconocido"}"
    }

    private fun exceptionMessage(e: Exception): String = when (e) {
        is SocketTimeoutException -> "Tiempo de espera agotado. Verifica tu conexión."
        is ConnectException -> "Sin conexión. Verifica tu internet."
        is HttpException -> httpErrorMessage(e.code(), e.message())
        else -> "Error: ${e.message ?: "Error desconocido"}"
    }
}