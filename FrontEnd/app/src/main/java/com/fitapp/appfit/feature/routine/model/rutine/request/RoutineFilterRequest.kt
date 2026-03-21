package com.fitapp.appfit.feature.routine.model.rutine.request

import com.google.gson.annotations.SerializedName

data class RoutineFilterRequest(
    @SerializedName("sportId") val sportId: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("isActive") val isActive: Boolean? = null,
    @SerializedName("sortBy") val sortBy: String = "createdAt",
    @SerializedName("sortDirection") val sortDirection: String = "DESC"
)