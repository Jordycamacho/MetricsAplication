package com.fitapp.appfit.feature.routine.model.rutine.response

import com.google.gson.annotations.SerializedName

data class RoutineStatisticsResponse(
    @SerializedName("totalRoutines") val totalRoutines: Long,
    @SerializedName("activeRoutines") val activeRoutines: Long,
    @SerializedName("inactiveRoutines") val inactiveRoutines: Long
)