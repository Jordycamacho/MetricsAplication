package com.fitapp.appfit.feature.exercise.model.exercise.request

import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseType
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ExerciseFilterRequest(
    @SerializedName("search")
    val search: String? = null,

    @SerializedName("exerciseType")
    val exerciseType: ExerciseType? = null,

    @SerializedName("sportId")
    val sportId: Long? = null,

    @SerializedName("categoryId")
    val categoryId: Long? = null,

    @SerializedName("parameterId")
    val parameterId: Long? = null,

    @SerializedName("isActive")
    val isActive: Boolean? = null,

    @SerializedName("isPublic")
    val isPublic: Boolean? = null,

    @SerializedName("createdBy")
    val createdBy: Long? = null,

    @SerializedName("includePublic")
    val includePublic: Boolean? = true,

    @SerializedName("minRating")
    val minRating: Double? = null,

    @SerializedName("page")
    val page: Int = 0,

    @SerializedName("size")
    val size: Int = 20,

    @SerializedName("sortBy")
    val sortBy: String = "name",

    @SerializedName("direction")
    val direction: SortDirection = SortDirection.ASC,

    @SerializedName("sortByPopularity")
    val sortByPopularity: Boolean = false,

    @SerializedName("sortByRating")
    val sortByRating: Boolean = false,

    @SerializedName("sortFields")
    val sortFields: List<SortField> = emptyList()
) : Serializable {
    enum class SortDirection {
        ASC,
        DESC
    }

    data class SortField(
        @SerializedName("field")
        val field: String,

        @SerializedName("direction")
        val direction: SortDirection = SortDirection.ASC
    ) : Serializable
}