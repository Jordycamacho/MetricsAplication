package com.fitapp.appfit.response.routine

data class CreateRoutineRequest(
    val name: String,
    val description: String? = null,
    val sportId: Long? = null
)