package com.fitapp.appfit.feature.parameter.model.request

import com.google.gson.annotations.SerializedName

data class CustomParameterFilterRequest(
    @SerializedName("search") val search: String? = null,
    @SerializedName("parameterType") val parameterType: String? = null,
    @SerializedName("isGlobal") val isGlobal: Boolean? = null,
    @SerializedName("isActive") val isActive: Boolean? = null,
    @SerializedName("isFavorite") val isFavorite: Boolean? = null,
    @SerializedName("isTrackable") val isTrackable: Boolean? = null,
    @SerializedName("sportId") val sportId: Long? = null,
    @SerializedName("ownerId") val ownerId: Long? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("onlyMine") val onlyMine: Boolean = false,
    @SerializedName("includeGlobal") val includeGlobal: Boolean = true,
    @SerializedName("page") val page: Int = 0,
    @SerializedName("size") val size: Int = 20,
    @SerializedName("sortBy") val sortBy: String = "name",
    @SerializedName("direction") val direction: String = "ASC"
)