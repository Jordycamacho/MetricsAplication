package com.fitapp.appfit.response.routine.request

import com.google.gson.annotations.SerializedName

data class AddExerciseToRoutineRequest(
    @SerializedName("exerciseId") val exerciseId: Long,
    @SerializedName("sessionNumber") val sessionNumber: Int? = 1,
    @SerializedName("dayOfWeek") val dayOfWeek: String? = null,
    @SerializedName("sessionOrder") val sessionOrder: Int? = null,
    @SerializedName("restAfterExercise") val restAfterExercise: Int? = null,
    @SerializedName("targetParameters") val targetParameters: List<ExerciseParameterRequest>? = null,
    @SerializedName("sets") val sets: List<SetTemplateRequest>? = null
)

data class ExerciseParameterRequest(
    @SerializedName("parameterId") val parameterId: Long,
    @SerializedName("numericValue") val numericValue: Double? = null,
    @SerializedName("integerValue") val integerValue: Int? = null,
    @SerializedName("durationValue") val durationValue: Long? = null,
    @SerializedName("stringValue") val stringValue: String? = null,
    @SerializedName("minValue") val minValue: Double? = null,
    @SerializedName("maxValue") val maxValue: Double? = null,
    @SerializedName("defaultValue") val defaultValue: Double? = null
)

data class SetTemplateRequest(
    @SerializedName("position") val position: Int,
    @SerializedName("subSetNumber") val subSetNumber: Int? = null,
    @SerializedName("groupId") val groupId: String? = null,
    @SerializedName("setType") val setType: String? = null,
    @SerializedName("restAfterSet") val restAfterSet: Int? = null,
    @SerializedName("parameters") val parameters: List<SetParameterRequest>? = null
)

data class SetParameterRequest(
    @SerializedName("parameterId") val parameterId: Long,
    @SerializedName("numericValue") val numericValue: Double? = null,
    @SerializedName("durationValue") val durationValue: Long? = null,
    @SerializedName("integerValue") val integerValue: Int? = null,
    @SerializedName("minValue") val minValue: Double? = null,
    @SerializedName("maxValue") val maxValue: Double? = null
)