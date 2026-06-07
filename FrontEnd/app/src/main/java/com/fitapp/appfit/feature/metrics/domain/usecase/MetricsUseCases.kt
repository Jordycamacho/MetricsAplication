package com.fitapp.appfit.feature.metrics.domain.usecase

import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.metrics.domain.model.ConsistencyStats
import com.fitapp.appfit.feature.metrics.domain.model.MetricsOverview
import com.fitapp.appfit.feature.metrics.domain.model.PersonalRecordItem
import com.fitapp.appfit.feature.metrics.domain.model.SessionComparison
import com.fitapp.appfit.feature.metrics.domain.model.SessionHistoryFilter
import com.fitapp.appfit.feature.metrics.domain.model.WeekActivity
import com.fitapp.appfit.feature.metrics.domain.model.WeeklyVolumePoint
import com.fitapp.appfit.feature.metrics.domain.repository.IMetricsReadRepository
import com.fitapp.appfit.feature.metrics.domain.util.DaySessionLabelFormatter
import com.fitapp.appfit.feature.workout.model.response.SessionExerciseResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse
import com.fitapp.appfit.shared.model.PageResponse
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

class GetSessionHistoryUseCase(private val repository: IMetricsReadRepository) {
    suspend operator fun invoke(filter: SessionHistoryFilter = SessionHistoryFilter()) =
        repository.getWorkoutHistory(filter)
}

class GetSessionDetailUseCase(private val repository: IMetricsReadRepository) {
    suspend operator fun invoke(sessionId: Long) = repository.getWorkoutSessionDetails(sessionId)
}

class DeleteSessionUseCase(private val repository: IMetricsReadRepository) {
    suspend operator fun invoke(sessionId: Long) = repository.deleteWorkoutSession(sessionId)
}

class GetTotalVolumeUseCase(private val repository: IMetricsReadRepository) {
    suspend operator fun invoke() = repository.getTotalVolume()
}

class GetMetricsOverviewUseCase(private val repository: IMetricsReadRepository) {

    private val sessionFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    suspend operator fun invoke(): MetricsOverview {
        val today = LocalDate.now()
        val from = today.minusWeeks(12).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val to = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val history = repository.getWorkoutHistory(
            SessionHistoryFilter(fromDate = from, toDate = to, page = 0, size = 200)
        )
        val sessions = (history as? Resource.Success)?.data?.content.orEmpty()
        val weekStats = GetConsistencyStatsUseCase.computeFromSessions(sessions, today)
        val totalVolume = (repository.getTotalVolume() as? Resource.Success)?.data
        val lastSession = (repository.getRecentWorkouts(1) as? Resource.Success)?.data?.content?.firstOrNull()

        return MetricsOverview(
            weeklySessions = weekStats.sessionsThisWeek,
            weeklyVolumeKg = weekStats.volumeThisWeekKg,
            streakWeeks = weekStats.streakWeeks,
            totalVolumeKg = totalVolume,
            lastSession = lastSession
        )
    }
}

class GetConsistencyStatsUseCase(private val repository: IMetricsReadRepository) {

    suspend operator fun invoke(): ConsistencyStats {
        val today = LocalDate.now()
        val from = today.minusWeeks(12).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val history = repository.getWorkoutHistory(
            SessionHistoryFilter(fromDate = from, toDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE), size = 200)
        )
        val sessions = (history as? Resource.Success)?.data?.content.orEmpty()
        return computeFullStats(sessions, today)
    }

    companion object {
        private val sessionFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        private val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")

        data class BasicConsistency(
            val sessionsThisWeek: Int,
            val volumeThisWeekKg: Double,
            val streakWeeks: Int
        )

        fun computeFromSessions(
            sessions: List<WorkoutSessionSummaryResponse>,
            today: LocalDate
        ): BasicConsistency {
            val weekStart = today.with(DayOfWeek.MONDAY)
            val weekSessions = sessions.filter { session ->
                parseSessionDate(session.startTime)?.let { it in weekStart..today } == true
            }
            return BasicConsistency(
                sessionsThisWeek = weekSessions.size,
                volumeThisWeekKg = weekSessions.sumOf { it.totalVolume ?: 0.0 },
                streakWeeks = computeStreakWeeks(sessions, today)
            )
        }

        fun computeFullStats(
            sessions: List<WorkoutSessionSummaryResponse>,
            today: LocalDate
        ): ConsistencyStats {
            val basic = computeFromSessions(sessions, today)
            val fourWeeksAgo = today.minusWeeks(4)
            val recent = sessions.filter { session ->
                parseSessionDate(session.startTime)?.let { it >= fourWeeksAgo } == true
            }
            val activeDays = recent.mapNotNull { parseSessionDate(it.startTime) }.toSet().size

            val activityByWeek = (0 until 4).map { offset ->
                val weekStart = today.minusWeeks(offset.toLong()).with(DayOfWeek.MONDAY)
                val weekEnd = weekStart.plusDays(6)
                val daysInWeek = recent.mapNotNull { parseSessionDate(it.startTime) }
                    .count { it in weekStart..weekEnd }
                WeekActivity(
                    weekLabel = weekStart.format(DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))),
                    daysTrained = daysInWeek
                )
            }.reversed()

            return ConsistencyStats(
                streakWeeks = basic.streakWeeks,
                sessionsLast4Weeks = recent.size,
                activeDaysLast4Weeks = activeDays,
                activityByWeek = activityByWeek
            )
        }

        fun buildWeekActivityDots(
            sessions: List<WorkoutSessionSummaryResponse>,
            today: LocalDate
        ): List<com.fitapp.appfit.feature.home.domain.DayActivity> {
            val weekStart = today.with(DayOfWeek.MONDAY)
            val workoutDates = sessions.mapNotNull { parseSessionDate(it.startTime) }.toSet()
            return (0..6).map { offset ->
                val date = weekStart.plusDays(offset.toLong())
                com.fitapp.appfit.feature.home.domain.DayActivity(
                    dayLabel = dayLabels[offset],
                    isToday = date == today,
                    hasWorkout = date in workoutDates
                )
            }
        }

        private fun computeStreakWeeks(
            sessions: List<WorkoutSessionSummaryResponse>,
            today: LocalDate
        ): Int {
            val weeksWithWorkout = sessions
                .mapNotNull { parseSessionDate(it.startTime) }
                .map { date -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
                .toSet()
            if (weeksWithWorkout.isEmpty()) return 0
            var streak = 0
            var week = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            while (week in weeksWithWorkout) {
                streak++
                week = week.minusWeeks(1)
            }
            return streak
        }

        private fun parseSessionDate(startTime: String): LocalDate? = try {
            LocalDateTime.parse(startTime, sessionFormatter).toLocalDate()
        } catch (_: Exception) {
            null
        }
    }
}

class GetWeeklyVolumeUseCase(private val repository: IMetricsReadRepository) {

    suspend operator fun invoke(routineId: Long? = null): List<WeeklyVolumePoint> {
        val today = LocalDate.now()
        val from = today.minusWeeks(12).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val history = repository.getWorkoutHistory(
            SessionHistoryFilter(routineId = routineId, fromDate = from, size = 200)
        )
        val sessions = (history as? Resource.Success)?.data?.content.orEmpty()

        val grouped = sessions
            .mapNotNull { session ->
                val date = parseDate(session.startTime) ?: return@mapNotNull null
                val weekStart = date.with(DayOfWeek.MONDAY)
                weekStart to session
            }
            .groupBy({ it.first }, { it.second })

        val sortedWeeks = grouped.keys.sortedDescending()
        return sortedWeeks.mapIndexed { index, weekStart ->
            val weekSessions = grouped[weekStart].orEmpty()
            val volume = weekSessions.sumOf { it.totalVolume ?: 0.0 }
            val prevVolume = sortedWeeks.getOrNull(index + 1)?.let { prevWeek ->
                grouped[prevWeek]?.sumOf { it.totalVolume ?: 0.0 }
            }
            WeeklyVolumePoint(
                weekKey = weekStart.toString(),
                weekLabel = weekStart.format(DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))),
                sessionsCount = weekSessions.size,
                volumeKg = volume,
                deltaVolumeKg = prevVolume?.let { volume - it }
            )
        }
    }

    private fun parseDate(iso: String): LocalDate? = try {
        LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()
    } catch (_: Exception) {
        null
    }
}

class CompareSessionsUseCase(private val repository: IMetricsReadRepository) {

    suspend operator fun invoke(current: WorkoutSessionResponse): Pair<SessionComparison?, List<SessionExerciseResponse>> {
        val currentStart = parseIso(current.startTime) ?: return null to emptyList()

        val history = repository.getWorkoutHistory(
            SessionHistoryFilter(routineId = current.routineId, size = 50)
        )
        if (history !is Resource.Success) return null to emptyList()

        val sessions = history.data?.content.orEmpty()
        val previousSummary = sessions
            .filter { summary ->
                val start = parseIso(summary.startTime) ?: return@filter false
                start.isBefore(currentStart) && matchesSameDaySession(current, summary)
            }
            .maxByOrNull { parseIso(it.startTime)!! }

        if (previousSummary == null) return null to emptyList()

        val currentSetCount = current.exercises?.sumOf { it.sets?.size ?: 0 } ?: 0
        val comparison = SessionComparison(
            previousSessionDate = previousSummary.startTime,
            volumeDifference = (current.totalVolume ?: 0.0) - (previousSummary.totalVolume ?: 0.0),
            durationDifference = (current.durationSeconds ?: 0L) - (previousSummary.durationSeconds ?: 0L),
            setsDifference = currentSetCount - previousSummary.setCount
        )

        val prevDetail = repository.getWorkoutSessionDetails(previousSummary.id)
        val prevExercises = if (prevDetail is Resource.Success) {
            prevDetail.data?.exercises.orEmpty()
        } else {
            emptyList()
        }

        return comparison to prevExercises
    }

    private fun matchesSameDaySession(
        current: WorkoutSessionResponse,
        summary: WorkoutSessionSummaryResponse
    ): Boolean {
        val currentDay = current.dayOfWeek
        val summaryDay = summary.dayOfWeek
        val currentSession = current.sessionNumber
        val summarySession = summary.sessionNumber

        return when {
            currentDay != null && summaryDay != null -> currentDay == summaryDay
            currentSession != null && summarySession != null -> currentSession == summarySession
            else -> true
        }
    }

    private fun parseIso(iso: String): LocalDateTime? = try {
        LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    } catch (_: Exception) {
        null
    }
}

class GetPersonalRecordsUseCase(private val repository: IMetricsReadRepository) {

    suspend operator fun invoke(maxSessions: Int = 30): List<PersonalRecordItem> {
        val history = repository.getWorkoutHistory(SessionHistoryFilter(size = maxSessions))
        if (history !is Resource.Success) return emptyList()

        val records = mutableListOf<PersonalRecordItem>()
        for (summary in history.data?.content.orEmpty()) {
            val detail = repository.getWorkoutSessionDetails(summary.id)
            if (detail !is Resource.Success) continue
            val session = detail.data ?: continue
            session.exercises.orEmpty().forEach { exercise ->
                exercise.sets.orEmpty().forEach { set ->
                    set.parameters.orEmpty().filter { it.isPersonalRecord == true }.forEach { param ->
                        val valueLabel = formatParamValue(param)
                        records.add(
                            PersonalRecordItem(
                                exerciseName = exercise.exerciseName ?: "Ejercicio",
                                parameterName = param.parameterName,
                                valueLabel = valueLabel,
                                achievedAt = summary.startTime,
                                sessionId = summary.id
                            )
                        )
                    }
                }
            }
        }
        return records.distinctBy { "${it.exerciseName}_${it.parameterName}_${it.valueLabel}" }
    }

    private fun formatParamValue(param: com.fitapp.appfit.feature.workout.model.response.SetExecutionParameterResponse): String {
        return when {
            param.numericValue != null && param.integerValue != null ->
                "${param.numericValue} ${param.unit ?: "kg"} × ${param.integerValue}"
            param.numericValue != null -> "${param.numericValue} ${param.unit ?: ""}"
            param.integerValue != null -> "${param.integerValue} ${param.unit ?: ""}"
            else -> "Récord"
        }
    }
}

class ResolveDaySessionLabelUseCase {
    operator fun invoke(
        dayOfWeek: String?,
        sessionNumber: Int?,
        dayLabel: String? = null
    ) = DaySessionLabelFormatter.from(dayOfWeek, sessionNumber, dayLabel)
}
