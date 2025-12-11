package com.fitapp.appfit.response.routine.request

data class RoutineFilterRequest(
    val sportId: Long? = null,
    val name: String? = null,
    val isActive: Boolean? = null,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC"
)