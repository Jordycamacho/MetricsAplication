package com.fitapp.appfit.feature.marketplace.data

import com.fitapp.appfit.core.network.ApiClient
import com.fitapp.appfit.feature.marketplace.model.request.AddPackageItemRequest
import com.fitapp.appfit.feature.marketplace.model.request.CreatePackageRequest
import com.fitapp.appfit.feature.marketplace.model.request.UpdatePackageRequest
import com.fitapp.appfit.feature.marketplace.model.response.PackageResponse
import com.fitapp.appfit.feature.marketplace.model.response.PackageSummaryResponse
import com.fitapp.appfit.feature.marketplace.model.response.PackageItemResponse
import com.fitapp.appfit.shared.model.PageResponse
import retrofit2.Response
import retrofit2.http.*

interface PackageService {

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @POST("api/packages")
    suspend fun createPackage(@Body request: CreatePackageRequest): Response<PackageResponse>

    @GET("api/packages/{id}")
    suspend fun getPackageById(@Path("id") id: Long): Response<PackageResponse>

    @GET("api/packages/by-slug/{slug}")
    suspend fun getPackageBySlug(@Path("slug") slug: String): Response<PackageResponse>

    @PUT("api/packages/{id}")
    suspend fun updatePackage(
        @Path("id") id: Long,
        @Body request: UpdatePackageRequest
    ): Response<PackageResponse>

    @DELETE("api/packages/{id}")
    suspend fun deletePackage(@Path("id") id: Long): Response<Unit>

    // ── Búsqueda y listados ───────────────────────────────────────────────────

    @GET("api/packages/search")
    suspend fun searchMarketplace(
        @Query("search") search: String? = null,
        @Query("packageType") packageType: String? = null,
        @Query("isFree") isFree: Boolean? = null,
        @Query("minRating") minRating: Double? = null,
        @Query("requiresSubscription") requiresSubscription: String? = null,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDirection") sortDirection: String = "DESC",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PageResponse<PackageSummaryResponse>>

    @GET("api/packages/official")
    suspend fun getOfficialPackages(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PageResponse<PackageSummaryResponse>>

    @GET("api/packages/creator/{userId}")
    suspend fun getUserPackages(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PageResponse<PackageSummaryResponse>>

    @GET("api/packages/my-purchases")
    suspend fun getUserPurchasedPackages(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PageResponse<PackageSummaryResponse>>

    // ── Items ─────────────────────────────────────────────────────────────────

    @POST("api/packages/{packageId}/items")
    suspend fun addItemToPackage(
        @Path("packageId") packageId: Long,
        @Body request: AddPackageItemRequest
    ): Response<PackageItemResponse>

    @DELETE("api/packages/{packageId}/items/{itemId}")
    suspend fun removeItemFromPackage(
        @Path("packageId") packageId: Long,
        @Path("itemId") itemId: Long
    ): Response<Unit>

    @PUT("api/packages/{packageId}/reorder")
    suspend fun reorderPackageItems(
        @Path("packageId") packageId: Long,
        @Body itemIds: List<Long>
    ): Response<PackageResponse>

    // ── Estado ────────────────────────────────────────────────────────────────

    @POST("api/packages/{id}/publish")
    suspend fun publishPackage(@Path("id") id: Long): Response<Unit>

    @POST("api/packages/{id}/deprecate")
    suspend fun deprecatePackage(
        @Path("id") id: Long,
        @Query("reason") reason: String?
    ): Response<Unit>

    @POST("api/packages/{id}/suspend")
    suspend fun suspendPackage(
        @Path("id") id: Long,
        @Query("reason") reason: String?
    ): Response<Unit>

    @POST("api/packages/{id}/unsuspend")
    suspend fun unsuspendPackage(@Path("id") id: Long): Response<Unit>

    // ── Especiales ────────────────────────────────────────────────────────────

    @GET("api/packages/trending")
    suspend fun getTrendingPackages(
        @Query("limit") limit: Int = 10
    ): Response<List<PackageSummaryResponse>>

    @GET("api/packages/top-rated")
    suspend fun getTopRatedPackages(
        @Query("limit") limit: Int = 10
    ): Response<List<PackageSummaryResponse>>

    @GET("api/packages/{id}/statistics")
    suspend fun getPackageStatistics(
        @Path("id") id: Long
    ): Response<PackageStatisticsResponse>

    // ── Interacciones ─────────────────────────────────────────────────────────

    @POST("api/packages/{id}/download")
    suspend fun downloadPackage(@Path("id") id: Long): Response<Unit>

    @POST("api/packages/{id}/rate")
    suspend fun ratePackage(
        @Path("id") id: Long,
        @Query("rating") rating: Double
    ): Response<Unit>

    @POST("api/packages/{id}/purchase")
    suspend fun purchasePackage(@Path("id") id: Long): Response<Unit>

    companion object {
        val instance: PackageService by lazy {
            ApiClient.instance.create(PackageService::class.java)
        }
    }
}

data class PackageStatisticsResponse(
    val packageId: Long,
    val totalDownloads: Int,
    val averageRating: Double?,
    val totalRatings: Int,
    val itemCount: Int,
    val lastUpdated: String?
)