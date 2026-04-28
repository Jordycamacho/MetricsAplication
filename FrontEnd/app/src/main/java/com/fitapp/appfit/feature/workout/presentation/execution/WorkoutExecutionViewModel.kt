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
import kotlinx.coroutines.launch

class WorkoutExecutionViewModel(
    private val saveWorkoutSessionUseCase: SaveWorkoutSessionUseCase,
    private val loadLastExerciseValuesUseCase: LoadLastExerciseValuesUseCase,
    private val lastWorkoutValuesApplier: LastWorkoutValuesApplier
) : ViewModel() {

    companion object {
        private const val TAG = "WorkoutExecutionViewModel"
    }

    // Estado: la rutina final (con o sin valores anteriores)
    private val _routineWithValuesState = MutableLiveData<Resource<RoutineResponse>>()
    val routineWithValuesState: LiveData<Resource<RoutineResponse>> = _routineWithValuesState

    // Estado: guardado de sesión
    private val _saveSessionState = MutableLiveData<Resource<Long>>()
    val saveSessionState: LiveData<Resource<Long>> = _saveSessionState

    /**
     * Carga los últimos valores y los aplica a la rutina.
     * Emite siempre un Resource<RoutineResponse> (si falla, emite la rutina original)
     */
    fun loadAndApplyLastValues(routine: RoutineResponse) {
        Log.i(TAG, "LOAD_AND_APPLY_LAST_VALUES | routineId=${routine.id}")
        _routineWithValuesState.value = Resource.Loading()

        viewModelScope.launch {
            try {
                val result = loadLastExerciseValuesUseCase(routine.id)

                when (result) {
                    is Resource.Success -> {
                        val lastValues = result.data ?: emptyMap()
                        Log.i(TAG, "LAST_VALUES_LOADED | count=${lastValues.size}")

                        val routineWithValues = lastWorkoutValuesApplier.applyValuesToRoutine(
                            routine,
                            lastValues
                        )
                        _routineWithValuesState.value = Resource.Success(routineWithValues)
                    }
                    is Resource.Error -> {
                        Log.w(TAG, "LOAD_FAILED | error=${result.message}")
                        // Emitimos la rutina original para que el entrenamiento continúe
                        _routineWithValuesState.value = Resource.Success(routine)
                    }
                    else -> {} // Loading ya estaba seteado
                }
            } catch (e: Exception) {
                Log.e(TAG, "EXCEPTION_LOADING_VALUES | error=${e.message}", e)
                _routineWithValuesState.value = Resource.Error(e.message ?: "Error desconocido")
            }
        }
    }

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

    /**
     * Resetea el estado de guardado para permitir nuevos intentos.
     */
    fun resetSaveState() {
        Log.d(TAG, "RESET_SAVE_STATE")
        _saveSessionState.value = Resource.Error("")
    }
}