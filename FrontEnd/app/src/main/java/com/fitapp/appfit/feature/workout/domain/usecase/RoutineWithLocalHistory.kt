package com.fitapp.appfit.feature.workout.domain.usecase

import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse

data class RoutineWithLocalHistory(
    val routine: RoutineResponse,
    val appliedLocalHistory: Boolean
)
