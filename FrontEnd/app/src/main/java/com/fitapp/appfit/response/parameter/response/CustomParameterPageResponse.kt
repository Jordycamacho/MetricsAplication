package com.fitapp.appfit.response.parameter.response

import com.google.gson.annotations.SerializedName

data class CustomParameterPageResponse(
    @SerializedName("content") val content: List<CustomParameterResponse>,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("first") val first: Boolean,
    @SerializedName("last") val last: Boolean,
    @SerializedName("numberOfElements") val numberOfElements: Int
)