package com.fitapp.appfit.response.routine

data class CreateRoutineRequest(
    val name: String,
    val description: String?,
    val sportId: Long?
)