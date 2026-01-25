package com.fitapp.appfit.response.routine.response

import com.google.gson.annotations.SerializedName

data class RoutineExerciseResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("exerciseId") val exerciseId: Long,
    @SerializedName("routineId") val routineId: Long,
    @SerializedName("exerciseName") val exerciseName: String?,
    @SerializedName("position") val position: Int,
    @SerializedName("sessionNumber") val sessionNumber: Int?,
    @SerializedName("dayOfWeek") val dayOfWeek: String?,
    @SerializedName("sessionOrder") val sessionOrder: Int?,
    @SerializedName("restAfterExercise") val restAfterExercise: Int?,
    @SerializedName("sets") val sets: Int?,
    @SerializedName("targetParameters") val targetParameters: List<RoutineExerciseParameterResponse>?,
    @SerializedName("setsTemplate") val setsTemplate: List<RoutineSetTemplateResponse>?
)

data class RoutineExerciseParameterResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("parameterId") val parameterId: Long,
    @SerializedName("parameterName") val parameterName: String?,
    @SerializedName("parameterType") val parameterType: String?,
    @SerializedName("numericValue") val numericValue: Double?,
    @SerializedName("integerValue") val integerValue: Int?,
    @SerializedName("durationValue") val durationValue: Long?,
    @SerializedName("stringValue") val stringValue: String?,
    @SerializedName("minValue") val minValue: Double?,
    @SerializedName("maxValue") val maxValue: Double?,
    @SerializedName("defaultValue") val defaultValue: Double?
)

data class RoutineSetTemplateResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("position") val position: Int,
    @SerializedName("subSetNumber") val subSetNumber: Int?,
    @SerializedName("groupId") val groupId: String?,
    @SerializedName("setType") val setType: String?,
    @SerializedName("restAfterSet") val restAfterSet: Int?,
    @SerializedName("parameters") val parameters: List<RoutineSetParameterResponse>?
)

data class RoutineSetParameterResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("parameterId") val parameterId: Long,
    @SerializedName("numericValue") val numericValue: Double?,
    @SerializedName("durationValue") val durationValue: Long?,
    @SerializedName("integerValue") val integerValue: Int?,
    @SerializedName("minValue") val minValue: Double?,
    @SerializedName("maxValue") val maxValue: Double?
)