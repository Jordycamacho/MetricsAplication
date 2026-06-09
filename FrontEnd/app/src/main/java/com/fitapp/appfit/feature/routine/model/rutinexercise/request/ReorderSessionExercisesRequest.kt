package com.fitapp.appfit.feature.routine.model.rutinexercise.request

import com.google.gson.annotations.SerializedName

data class ReorderSessionExercisesRequest(
    @SerializedName("dayOfWeek") val dayOfWeek: String? = null,
    @SerializedName("sessionNumber") val sessionNumber: Int? = null,
    @SerializedName("exerciseIds") val exerciseIds: List<Long>
)
