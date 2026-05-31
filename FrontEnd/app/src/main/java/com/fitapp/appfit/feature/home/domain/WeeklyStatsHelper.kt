package com.fitapp.appfit.feature.home.domain

import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineSummaryResponse
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class WeeklyStats(
    val sessionsCount: Int,
    val volumeKg: Double,
    val streakWeeks: Int
)

data class DayActivity(
    val dayLabel: String,
    val isToday: Boolean,
    val hasWorkout: Boolean
)

object WeeklyStatsHelper {

    private val sessionFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")

    fun fromSessions(
        sessions: List<WorkoutSessionSummaryResponse>,
        today: LocalDate
    ): WeeklyStats {
        val weekStart = today.with(DayOfWeek.MONDAY)
        val weekSessions = sessions.filter { session ->
            parseSessionDate(session.startTime)?.let { it in weekStart..today } == true
        }
        return WeeklyStats(
            sessionsCount = weekSessions.size,
            volumeKg = weekSessions.sumOf { it.totalVolume ?: 0.0 },
            streakWeeks = computeStreakWeeks(sessions, today)
        )
    }

    fun buildWeekActivityDots(
        sessions: List<WorkoutSessionSummaryResponse>,
        today: LocalDate
    ): List<DayActivity> {
        val weekStart = today.with(DayOfWeek.MONDAY)
        val workoutDates = sessions.mapNotNull { parseSessionDate(it.startTime) }.toSet()

        return (0..6).map { offset ->
            val date = weekStart.plusDays(offset.toLong())
            DayActivity(
                dayLabel = dayLabels[offset],
                isToday = date == today,
                hasWorkout = date in workoutDates
            )
        }
    }

    fun findPlannedForToday(
        activeRoutines: List<RoutineSummaryResponse>,
        today: LocalDate
    ): RoutineSummaryResponse? {
        val todayName = today.dayOfWeek.name
        return activeRoutines.firstOrNull { routine ->
            routine.isActive && routine.trainingDays.contains(todayName)
        }
    }

    fun isPlannedForToday(routine: RoutineSummaryResponse, today: LocalDate): Boolean {
        return routine.isActive && routine.trainingDays.contains(today.dayOfWeek.name)
    }

    fun formatLastUsed(lastUsedAt: String?): String? {
        if (lastUsedAt.isNullOrBlank()) return null
        return try {
            val usedDate = LocalDateTime.parse(lastUsedAt, sessionFormatter).toLocalDate()
            formatRelativeDate(usedDate)
        } catch (_: Exception) {
            null
        }
    }

    fun formatLastSessionSummary(session: WorkoutSessionSummaryResponse): String {
        val parts = mutableListOf<String>()
        parts.add(session.routineName)
        session.durationSeconds?.let { seconds ->
            val minutes = (seconds / 60).coerceAtLeast(1)
            parts.add("$minutes min")
        }
        session.totalVolume?.takeIf { it > 0 }?.let { volume ->
            parts.add(
                if (volume >= 1000) String.format(Locale.getDefault(), "%.1fk kg", volume / 1000)
                else String.format(Locale.getDefault(), "%.0f kg", volume)
            )
        }
        return parts.joinToString(" · ")
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

    private fun formatRelativeDate(date: LocalDate): String {
        val today = LocalDate.now()
        val days = ChronoUnit.DAYS.between(date, today)
        return when {
            days == 0L -> "Hoy"
            days == 1L -> "Ayer"
            days < 7 -> "Hace $days días"
            days < 30 -> "Hace ${days / 7} sem."
            else -> date.format(DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES")))
        }
    }

    private fun parseSessionDate(startTime: String): LocalDate? = try {
        LocalDateTime.parse(startTime, sessionFormatter).toLocalDate()
    } catch (_: Exception) {
        null
    }
}
