package com.fitapp.appfit.feature.marketplace.model.request

data class PackageFilterRequest (
    val search: String,
    val packageType: String, // SPORT_PACK, PARAMETER_PACK, ROUTINE_PACK, EXERCISE_PACK, MIXED
    val isFree: Boolean,
    val requiresSubscription: String, //FREE STANDARD PREMIUM
    val minRating: Double,
    val createdByUserId: Long,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC"
)
