package com.fitapp.appfit.feature.workout.model.response

import com.google.gson.annotations.SerializedName

data class WorkoutSessionResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("routineId")
    val routineId: Long,

    @SerializedName("routineName")
    val routineName: String?,

    @SerializedName("userId")
    val userId: Long,

    @SerializedName("startTime")
    val startTime: String,

    @SerializedName("endTime")
    val endTime: String,

    @SerializedName("durationSeconds")
    val durationSeconds: Long?,

    @SerializedName("performanceScore")
    val performanceScore: Int?,

    @SerializedName("totalVolume")
    val totalVolume: Double?,

    @SerializedName("exercises")
    val exercises: List<SessionExerciseResponse>?
)

data class SessionExerciseResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("exerciseId")
    val exerciseId: Long,

    @SerializedName("exerciseName")
    val exerciseName: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("startedAt")
    val startedAt: String?,

    @SerializedName("completedAt")
    val completedAt: String?,

    @SerializedName("personalNotes")
    val personalNotes: String?,

    @SerializedName("sets")
    val sets: List<SetExecutionResponse>?
)

data class SetExecutionResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("setTemplateId")
    val setTemplateId: Long?,

    @SerializedName("position")
    val position: Int,

    @SerializedName("setType")
    val setType: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("startedAt")
    val startedAt: String?,

    @SerializedName("completedAt")
    val completedAt: String?,

    @SerializedName("durationSeconds")
    val durationSeconds: Long?,

    @SerializedName("actualRestSeconds")
    val actualRestSeconds: Int?,

    @SerializedName("notes")
    val notes: String?,

    @SerializedName("volume")
    val volume: Double?,

    @SerializedName("parameters")
    val parameters: List<SetExecutionParameterResponse>?
)

data class SetExecutionParameterResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("parameterId")
    val parameterId: Long,

    @SerializedName("parameterName")
    val parameterName: String?,

    @SerializedName("parameterType")
    val parameterType: String?,

    @SerializedName("unit")
    val unit: String?,

    @SerializedName("numericValue")
    val numericValue: Double?,

    @SerializedName("integerValue")
    val integerValue: Int?,

    @SerializedName("durationValue")
    val durationValue: Long?,

    @SerializedName("stringValue")
    val stringValue: String?,

    @SerializedName("isPersonalRecord")
    val isPersonalRecord: Boolean?
)