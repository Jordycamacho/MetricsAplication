package com.fitapp.appfit.response.exercise.response


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ExercisePageResponse(
    @SerializedName("content")
    val content: List<ExerciseResponse>,

    @SerializedName("pageNumber")
    val pageNumber: Int,

    @SerializedName("pageSize")
    val pageSize: Int,

    @SerializedName("totalElements")
    val totalElements: Long,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("first")
    val first: Boolean,

    @SerializedName("last")
    val last: Boolean,

    @SerializedName("numberOfElements")
    val numberOfElements: Int,

    @SerializedName("appliedFilters")
    val appliedFilters: String?
) : Serializable