package com.fitapp.appfit.feature.exercise.model.exercise.request

import com.fitapp.appfit.feature.exercise.model.exercise.response.ExerciseType
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ExerciseRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("exerciseType")
    val exerciseType: ExerciseType,

    @SerializedName("sportIds")
    val sportIds: Set<Long>,

    @SerializedName("categoryIds")
    val categoryIds: Set<Long> = emptySet(),

    @SerializedName("supportedParameterIds")
    val supportedParameterIds: Set<Long> = emptySet(),

    @SerializedName("isPublic")
    val isPublic: Boolean = false
) : Serializable