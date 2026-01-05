package com.fitapp.appfit.response.category.response

import com.google.gson.annotations.SerializedName

data class ExerciseCategoryPageResponse(
    @SerializedName("content") val content: List<ExerciseCategoryResponse>,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("first") val first: Boolean,
    @SerializedName("last") val last: Boolean,
    @SerializedName("numberOfElements") val numberOfElements: Int
)