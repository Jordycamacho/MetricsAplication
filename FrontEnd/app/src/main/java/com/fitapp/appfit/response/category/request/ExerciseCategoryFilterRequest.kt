package com.fitapp.appfit.response.category.request

import com.google.gson.annotations.SerializedName

data class ExerciseCategoryFilterRequest(
    @SerializedName("search") val search: String? = null,
    @SerializedName("isPredefined") val isPredefined: Boolean? = null,
    @SerializedName("isActive") val isActive: Boolean? = null,
    @SerializedName("isPublic") val isPublic: Boolean? = null,
    @SerializedName("sportId") val sportId: Long? = null,
    @SerializedName("ownerId") val ownerId: Long? = null,
    @SerializedName("onlyMine") val onlyMine: Boolean = false,
    @SerializedName("includePredefined") val includePredefined: Boolean = true,
    @SerializedName("page") val page: Int = 0,
    @SerializedName("size") val size: Int = 20,
    @SerializedName("sortBy") val sortBy: String = "name",
    @SerializedName("direction") val direction: String = "ASC"
)