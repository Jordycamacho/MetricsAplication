package com.fitapp.appfit.feature.home.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.home.domain.DayActivity
import com.fitapp.appfit.feature.home.domain.WeeklyStatsHelper
import com.fitapp.appfit.feature.profile.data.UserRepository
import com.fitapp.appfit.feature.routine.data.RoutineRepository
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineSummaryResponse
import com.fitapp.appfit.feature.workout.data.repository.WorkoutRepositoryImpl
import com.fitapp.appfit.feature.workout.model.response.WorkoutSessionSummaryResponse
import com.fitapp.appfit.feature.workout.presentation.execution.manager.ActiveWorkoutCache
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ActiveWorkoutBanner(
    val routineId: Long,
    val startedAt: Long
)

data class HomeUiState(
    val userFirstName: String? = null,
    val activeWorkout: ActiveWorkoutBanner? = null,
    val heroRoutine: RoutineSummaryResponse? = null,
    val otherRoutines: List<RoutineSummaryResponse> = emptyList(),
    val weeklySessions: Int? = null,
    val weeklyVolumeKg: Double? = null,
    val streakWeeks: Int? = null,
    val activityDots: List<DayActivity> = emptyList(),
    val lastSession: WorkoutSessionSummaryResponse? = null,
    val plannedTodayRoutine: RoutineSummaryResponse? = null,
    val isLoadingRoutines: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val routineRepository = RoutineRepository(application)
    private val workoutRepository = WorkoutRepositoryImpl(application)
    private val userRepository = UserRepository()
    private val activeWorkoutCache = ActiveWorkoutCache(application)

    private val _uiState = MutableLiveData(HomeUiState(isLoadingRoutines = true))
    val uiState: LiveData<HomeUiState> = _uiState

    private var cachedHistorySessions: List<WorkoutSessionSummaryResponse> = emptyList()

    fun markRoutineAsUsed(routineId: Long) {
        viewModelScope.launch {
            routineRepository.markRoutineAsUsed(routineId)
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = (_uiState.value ?: HomeUiState()).copy(isLoadingRoutines = true)

            val today = LocalDate.now()
            val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
            val historyFrom = today.minusWeeks(12)

            val profileDeferred = async { userRepository.getMyProfile() }
            val routinesDeferred = async { routineRepository.getLastUsedRoutines(3) }
            val activeRoutinesDeferred = async { routineRepository.getActiveRoutines() }
            val historyDeferred = async {
                workoutRepository.getWorkoutHistory(
                    fromDate = historyFrom.format(dateFormatter),
                    toDate = today.format(dateFormatter),
                    page = 0,
                    size = 200
                )
            }
            val recentDeferred = async { workoutRepository.getRecentWorkouts(1) }

            val profileResult = profileDeferred.await()
            val routinesResult = routinesDeferred.await()
            val activeRoutinesResult = activeRoutinesDeferred.await()
            val historySessions = (historyDeferred.await() as? Resource.Success)?.data?.content.orEmpty()
            val recentResult = recentDeferred.await()

            val userName = (profileResult as? Resource.Success)?.data?.fullName
                ?.trim()
                ?.substringBefore(" ")
                ?.takeIf { it.isNotBlank() }

            val routines = (routinesResult as? Resource.Success)?.data.orEmpty()
            val activeRoutines = (activeRoutinesResult as? Resource.Success)?.data.orEmpty()
            val weeklyStats = WeeklyStatsHelper.fromSessions(historySessions, today)
            val lastSession = (recentResult as? Resource.Success)?.data?.content?.firstOrNull()

            val heroRoutine = routines.firstOrNull()
            val plannedToday = WeeklyStatsHelper.findPlannedForToday(activeRoutines, today)
                ?.takeIf { planned -> heroRoutine?.id != planned.id }

            val activeWorkout = activeWorkoutCache.getActiveRoutineIdOrNull()?.let { routineId ->
                ActiveWorkoutBanner(routineId, activeWorkoutCache.getStartedAt())
            }

            cachedHistorySessions = historySessions

            _uiState.value = HomeUiState(
                userFirstName = userName,
                activeWorkout = activeWorkout,
                heroRoutine = heroRoutine,
                otherRoutines = routines.drop(1),
                weeklySessions = weeklyStats.sessionsCount,
                weeklyVolumeKg = weeklyStats.volumeKg,
                streakWeeks = weeklyStats.streakWeeks,
                activityDots = WeeklyStatsHelper.buildWeekActivityDots(historySessions, today),
                lastSession = lastSession,
                plannedTodayRoutine = plannedToday,
                isLoadingRoutines = false
            )
        }
    }

    fun refreshActiveWorkoutBanner() {
        val current = _uiState.value ?: return
        val activeWorkout = activeWorkoutCache.getActiveRoutineIdOrNull()?.let { routineId ->
            ActiveWorkoutBanner(routineId, activeWorkoutCache.getStartedAt())
        }
        _uiState.value = current.copy(activeWorkout = activeWorkout)
    }

    fun refreshHomeAfterWorkout() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val routinesResult = routineRepository.getLastUsedRoutines(3)
            val recentResult = workoutRepository.getRecentWorkouts(1)
            val routines = (routinesResult as? Resource.Success)?.data.orEmpty()
            val weeklyStats = WeeklyStatsHelper.fromSessions(cachedHistorySessions, today)
            val lastSession = (recentResult as? Resource.Success)?.data?.content?.firstOrNull()
            val current = _uiState.value ?: HomeUiState()

            _uiState.value = current.copy(
                heroRoutine = routines.firstOrNull(),
                otherRoutines = routines.drop(1),
                weeklySessions = weeklyStats.sessionsCount,
                weeklyVolumeKg = weeklyStats.volumeKg,
                streakWeeks = weeklyStats.streakWeeks,
                activityDots = WeeklyStatsHelper.buildWeekActivityDots(cachedHistorySessions, today),
                lastSession = lastSession
            )
        }
    }
}
