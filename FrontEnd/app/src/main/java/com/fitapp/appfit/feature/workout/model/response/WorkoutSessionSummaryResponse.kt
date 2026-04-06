package com.fitapp.appfit.feature.workout.model.response

import com.google.gson.annotations.SerializedName

data class WorkoutSessionSummaryResponse(
    @SerializedName("id")
    val id: Long,

    @SerializedName("routineId")
    val routineId: Long,

    @SerializedName("routineName")
    val routineName: String,

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

    @SerializedName("exerciseCount")
    val exerciseCount: Int,

    @SerializedName("setCount")
    val setCount: Int
)