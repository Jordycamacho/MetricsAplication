package com.fitapp.appfit.response.sport.request

import com.google.gson.annotations.SerializedName

data class SportFilterRequest(
    @SerializedName("search") val search: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("isPredefined") val isPredefined: Boolean? = null,
    @SerializedName("sourceType") val sourceType: String? = null,
    @SerializedName("createdBy") val createdBy: Long? = null,
    @SerializedName("page") val page: Int = 0,
    @SerializedName("size") val size: Int = 10,
    @SerializedName("sortBy") val sortBy: String = "name",
    @SerializedName("direction") val direction: String = "ASC"
)