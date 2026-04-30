package com.fitapp.appfit.feature.workout.presentation.execution

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.workout.domain.manager.LastWorkoutValuesApplier
import com.fitapp.appfit.feature.workout.domain.usecase.LoadLastExerciseValuesUseCase
import com.fitapp.appfit.feature.workout.domain.usecase.SaveWorkoutSessionUseCase
import com.fitapp.appfit.feature.workout.presentation.execution.manager.ActiveWorkoutCache
import kotlinx.coroutines.launch

class WorkoutExecutionViewModel(
    private val saveWorkoutSessionUseCase: SaveWorkoutSessionUseCase,
    private val loadLastExerciseValuesUseCase: LoadLastExerciseValuesUseCase,
    private val lastWorkoutValuesApplier: LastWorkoutValuesApplier,
    val activeWorkoutCache: ActiveWorkoutCache
) : ViewModel() {

    companion object {
        private const val TAG = "WorkoutExecutionViewModel"
    }

    // Rutina final lista para mostrar (con o sin valores históricos aplicados)
    private val _routineWithValuesState = MutableLiveData<Resource<RoutineResponse>>()
    val routineWithValuesState: LiveData<Resource<RoutineResponse>> = _routineWithValuesState

    // Resultado del guardado de sesión
    private val _saveSessionState = MutableLiveData<Resource<Long>>()
    val saveSessionState: LiveData<Resource<Long>> = _saveSessionState

    /**
     * Estado restaurado del cache para que el Fragment lo aplique a
     * WorkoutCompletionState sin volver a pedir los datos al servidor.
     * Null = no hay sesión activa en cache para esta rutina.
     */
    private val _restoredCacheState = MutableLiveData<RestoredWorkoutState?>()
    val restoredCacheState: LiveData<RestoredWorkoutState?> = _restoredCacheState

    // ── Cache: persistencia de sesión activa ──────────────────────────────────

    /**
     * Comprueba si hay una sesión activa guardada para esta rutina.
     * Si la hay, emite RestoredWorkoutState para que el Fragment la aplique.
     */
    fun checkAndRestoreActiveSession(routineId: Long) {
        if (activeWorkoutCache.hasActiveWorkout(routineId)) {
            Log.i(TAG, "RESTORING_ACTIVE_SESSION | routineId=$routineId")
            val paramState = activeWorkoutCache.loadParamState()
            val completedSets = activeWorkoutCache.loadCompletedSets()
            val startedAt = activeWorkoutCache.getStartedAt()

            if (paramState.isNotEmpty() || completedSets.isNotEmpty()) {
                _restoredCacheState.value = RestoredWorkoutState(
                    paramState = paramState,
                    completedSetIds = completedSets,
                    startedAt = startedAt
                )
                Log.i(TAG, "SESSION_RESTORED | sets=${completedSets.size} | paramEntries=${paramState.size}")
                return
            }
        }
        _restoredCacheState.value = null
    }

    fun persistParamState(state: Map<Long, Map<String, Any?>>) {
        activeWorkoutCache.saveParamState(state)
    }

    fun persistCompletedSets(completedSetIds: Set<Long>) {
        activeWorkoutCache.saveCompletedSets(completedSetIds)
    }

    // ── Last values ───────────────────────────────────────────────────────────

    /**
     * Carga los últimos valores del backend y los aplica a la rutina.
     * Si falla, emite la rutina original para que el entrenamiento no se bloquee.
     */
    fun loadAndApplyLastValues(routine: RoutineResponse) {
        Log.i(TAG, "LOAD_AND_APPLY_LAST_VALUES | routineId=${routine.id}")
        _routineWithValuesState.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val result = loadLastExerciseValuesUseCase(routine.id)
                val lastValues = (result as? Resource.Success)?.data ?: emptyMap()

                if (lastValues.isEmpty()) {
                    Log.i(TAG, "NO_LAST_VALUES | emitting original routine")
                } else {
                    Log.i(TAG, "LAST_VALUES_LOADED | count=${lastValues.size}")
                }

                val routineWithValues = lastWorkoutValuesApplier.applyValuesToRoutine(routine, lastValues)
                _routineWithValuesState.value = Resource.Success(routineWithValues)

            } catch (e: Exception) {
                Log.e(TAG, "EXCEPTION_LOADING_VALUES | error=${e.message}", e)
                _routineWithValuesState.value = Resource.Success(routine)
            }
        }
    }

    // ── Save session ──────────────────────────────────────────────────────────

    fun saveWorkoutSession(
        routineId: Long,
        userId: String,
        setParamState: Map<Long, Map<String, Any?>>,
        setCompletionState: Map<Long, Boolean>,
        startedAt: Long,
        finishedAt: Long,
        performanceScore: Int? = null
    ) {
        Log.i(TAG, "SAVE_WORKOUT_SESSION | routineId=$routineId | userId=$userId")
        _saveSessionState.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val result = saveWorkoutSessionUseCase(
                    routineId = routineId,
                    userId = userId,
                    setParamState = setParamState,
                    setCompletionState = setCompletionState,
                    startedAt = startedAt,
                    finishedAt = finishedAt,
                    performanceScore = performanceScore
                )

                if (result.isSuccess) {
                    val sessionId = result.getOrNull() ?: -1L
                    Log.i(TAG, "SESSION_SAVED | sessionId=$sessionId")
                    activeWorkoutCache.clear()
                    _saveSessionState.value = Resource.Success(sessionId)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e(TAG, "SAVE_FAILED | error=$error")
                    _saveSessionState.value = Resource.Error(error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "EXCEPTION_SAVING_SESSION | error=${e.message}", e)
                _saveSessionState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetSaveState() {
        _saveSessionState.value = Resource.Error("")
    }

    // ── Data class ────────────────────────────────────────────────────────────

    data class RestoredWorkoutState(
        val paramState: Map<Long, Map<String, Any?>>,
        val completedSetIds: Set<Long>,
        val startedAt: Long
    )
}