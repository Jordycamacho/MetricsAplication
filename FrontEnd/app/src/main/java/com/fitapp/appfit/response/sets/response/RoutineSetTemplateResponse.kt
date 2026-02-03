package com.fitapp.appfit.response.sets.response

data class RoutineSetTemplateResponse(
    val id: Long,
    val routineExerciseId: Long,
    val position: Int,
    val subSetNumber: Int?,
    val groupId: String?,
    val setType: String?,
    val restAfterSet: Int?,
    val parameters: List<RoutineSetParameterResponse>?,
)