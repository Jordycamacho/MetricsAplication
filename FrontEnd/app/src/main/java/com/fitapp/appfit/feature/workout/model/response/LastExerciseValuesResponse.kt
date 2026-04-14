package com.fitapp.appfit.feature.workout.model.response

import com.google.gson.annotations.SerializedName

data class LastExerciseValuesResponse(
    @SerializedName("exerciseId")
    val exerciseId: Long,

    @SerializedName("exerciseName")
    val exerciseName: String,

    @SerializedName("lastWorkoutDate")
    val lastWorkoutDate: String,

    @SerializedName("sessionId")
    val sessionId: Long,

    @SerializedName("sets")
    val sets: List<LastSetValue>
) {
    data class LastSetValue(
        @SerializedName("position")
        val position: Int,

        @SerializedName("setType")
        val setType: String?,

        @SerializedName("parameters")
        val parameters: List<ParameterValue>
    )

    data class ParameterValue(
        @SerializedName("parameterId")
        val parameterId: Long,

        @SerializedName("parameterName")
        val parameterName: String,

        @SerializedName("parameterType")
        val parameterType: String,

        @SerializedName("unit")
        val unit: String?,

        @SerializedName("numericValue")
        val numericValue: Double?,

        @SerializedName("integerValue")
        val integerValue: Int?,

        @SerializedName("durationValue")
        val durationValue: Long?,

        @SerializedName("stringValue")
        val stringValue: String?
    )
}