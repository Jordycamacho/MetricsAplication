package com.fitapp.appfit.response.routine.response


import com.google.gson.annotations.SerializedName

data class RoutineStatisticsResponse(
    @SerializedName("totalRoutines") val totalRoutines: Long,
    @SerializedName("activeRoutines") val activeRoutines: Long,
    @SerializedName("inactiveRoutines") val inactiveRoutines: Long
)