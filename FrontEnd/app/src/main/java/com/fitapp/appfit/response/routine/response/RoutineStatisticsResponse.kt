package com.fitapp.appfit.response.routine.response

data class RoutineStatisticsResponse(
    val totalRoutines: Long,
    val activeRoutines: Long,
    val inactiveRoutines: Long,
)
