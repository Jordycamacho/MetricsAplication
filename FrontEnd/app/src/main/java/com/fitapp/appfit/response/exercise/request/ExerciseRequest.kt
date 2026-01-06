package com.fitapp.appfit.response.exercise.request

import com.google.gson.annotations.SerializedName
import com.fitapp.appfit.response.exercise.response.ExerciseType
import java.io.Serializable

data class ExerciseRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("exerciseType")
    val exerciseType: ExerciseType,

    @SerializedName("sportId")
    val sportId: Long,

    @SerializedName("categoryIds")
    val categoryIds: Set<Long> = emptySet(),

    @SerializedName("supportedParameterIds")
    val supportedParameterIds: Set<Long> = emptySet(),

    @SerializedName("isPublic")
    val isPublic: Boolean = false
) : Serializable