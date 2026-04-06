package com.fitapp.appfit.feature.workout.model.request

import com.google.gson.annotations.SerializedName

data class SaveWorkoutSessionRequest(
    @SerializedName("routineId")
    val routineId: Long,

    @SerializedName("startTime")
    val startTime: String, // ISO-8601 format: "2026-04-02T10:00:00"

    @SerializedName("endTime")
    val endTime: String,

    @SerializedName("performanceScore")
    val performanceScore: Int? = null,

    @SerializedName("setExecutions")
    val setExecutions: List<SetExecutionRequest>
) {

    data class SetExecutionRequest(
        @SerializedName("setTemplateId")
        val setTemplateId: Long?,

        @SerializedName("exerciseId")
        val exerciseId: Long,

        @SerializedName("position")
        val position: Int,

        @SerializedName("setType")
        val setType: String? = "NORMAL",

        @SerializedName("status")
        val status: String? = "COMPLETED",

        @SerializedName("startedAt")
        val startedAt: String? = null,

        @SerializedName("completedAt")
        val completedAt: String? = null,

        @SerializedName("actualRestSeconds")
        val actualRestSeconds: Int? = null,

        @SerializedName("notes")
        val notes: String? = null,

        @SerializedName("parameters")
        val parameters: List<ParameterValueRequest>
    )

    data class ParameterValueRequest(
        @SerializedName("parameterId")
        val parameterId: Long,

        @SerializedName("numericValue")
        val numericValue: Double? = null,

        @SerializedName("integerValue")
        val integerValue: Int? = null,

        @SerializedName("durationValue")
        val durationValue: Long? = null,

        @SerializedName("stringValue")
        val stringValue: String? = null
    )
}