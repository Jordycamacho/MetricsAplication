package com.fitapp.appfit.feature.metrics.domain.model

import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse

data class MetricsOverview(
    val weeklySessions: Int,
    val weeklyVolumeKg: Double,
    val streakWeeks: Int,
    val totalVolumeKg: Double?,
    val lastSession: WorkoutSessionSummaryResponse?
)

data class DaySessionLabel(
    val dayOfWeek: String? = null,
    val sessionNumber: Int? = null,
    val displayLabel: String
)

data class WeeklyVolumePoint(
    val weekKey: String,
    val weekLabel: String,
    val sessionsCount: Int,
    val volumeKg: Double,
    val deltaVolumeKg: Double?
)

data class ConsistencyStats(
    val streakWeeks: Int,
    val sessionsLast4Weeks: Int,
    val activeDaysLast4Weeks: Int,
    val activityByWeek: List<WeekActivity>
)

data class WeekActivity(
    val weekLabel: String,
    val daysTrained: Int
)

data class PersonalRecordItem(
    val exerciseName: String,
    val parameterName: String?,
    val valueLabel: String,
    val achievedAt: String,
    val sessionId: Long
)

data class SessionHistoryFilter(
    val routineId: Long? = null,
    val fromDate: String? = null,
    val toDate: String? = null,
    val dayOfWeek: String? = null,
    val sessionNumber: Int? = null,
    val page: Int = 0,
    val size: Int = 20
)

data class SessionComparison(
    val previousSessionDate: String,
    val volumeDifference: Double,
    val durationDifference: Long,
    val setsDifference: Int
)
