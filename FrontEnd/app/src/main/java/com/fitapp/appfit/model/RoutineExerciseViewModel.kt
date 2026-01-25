package com.fitapp.appfit.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.response.routine.request.AddExerciseToRoutineRequest
import com.fitapp.appfit.response.routine.response.RoutineExerciseResponse
import com.fitapp.appfit.service.RoutineExerciseService
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch
import android.util.Log
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import kotlinx.coroutines.delay

class RoutineExerciseViewModel : ViewModel() {

    private val _addExerciseState = MutableLiveData<Resource<RoutineExerciseResponse>>()
    val addExerciseState: LiveData<Resource<RoutineExerciseResponse>> get() = _addExerciseState

    private val _updateExerciseState = MutableLiveData<Resource<RoutineExerciseResponse>>()
    val updateExerciseState: LiveData<Resource<RoutineExerciseResponse>> get() = _updateExerciseState

    private val _removeExerciseState = MutableLiveData<Resource<Unit>>()
    val removeExerciseState: LiveData<Resource<Unit>> get() = _removeExerciseState

    private val _exercisesBySessionState = MutableLiveData<Resource<List<RoutineExerciseResponse>>>()
    val exercisesBySessionState: LiveData<Resource<List<RoutineExerciseResponse>>> get() = _exercisesBySessionState

    private val _exercisesByDayState = MutableLiveData<Resource<List<RoutineExerciseResponse>>>()
    val exercisesByDayState: LiveData<Resource<List<RoutineExerciseResponse>>> get() = _exercisesByDayState

    private val _reorderExercisesState = MutableLiveData<Resource<Unit>>()
    val reorderExercisesState: LiveData<Resource<Unit>> get() = _reorderExercisesState

    companion object {
        private const val TAG = "AddExercisesToRoutine"
    }
    fun addExerciseToRoutine(routineId: Long, request: AddExerciseToRoutineRequest) {
        viewModelScope.launch {
            _addExerciseState.value = Resource.Loading()
            try {
                val response = RoutineExerciseService.instance.addExerciseToRoutine(routineId, request)
                if (response.isSuccessful) {
                    _addExerciseState.value = Resource.Success(response.body()!!)
                } else {
                    _addExerciseState.value = Resource.Error(response.message())
                }
            } catch (e: Exception) {
                _addExerciseState.value = Resource.Error(e.message ?: "Error adding exercise")
            }
        }
    }

    fun updateExerciseInRoutine(routineId: Long, exerciseId: Long, request: AddExerciseToRoutineRequest) {
        viewModelScope.launch {
            _updateExerciseState.value = Resource.Loading()
            try {
                val response = RoutineExerciseService.instance.updateExerciseInRoutine(routineId, exerciseId, request)
                if (response.isSuccessful) {
                    _updateExerciseState.value = Resource.Success(response.body()!!)
                } else {
                    _updateExerciseState.value = Resource.Error(response.message())
                }
            } catch (e: Exception) {
                _updateExerciseState.value = Resource.Error(e.message ?: "Error updating exercise")
            }
        }
    }

    fun removeExerciseFromRoutine(routineId: Long, exerciseId: Long) {
        viewModelScope.launch {
            _removeExerciseState.value = Resource.Loading()
            try {
                val response = RoutineExerciseService.instance.removeExerciseFromRoutine(routineId, exerciseId)
                if (response.isSuccessful) {
                    _removeExerciseState.value = Resource.Success(Unit)
                } else {
                    _removeExerciseState.value = Resource.Error(response.message())
                }
            } catch (e: Exception) {
                _removeExerciseState.value = Resource.Error(e.message ?: "Error removing exercise")
            }
        }
    }

    fun getExercisesBySession(routineId: Long, sessionNumber: Int) {
        viewModelScope.launch {
            _exercisesBySessionState.value = Resource.Loading()
            try {
                val response = RoutineExerciseService.instance.getExercisesBySession(routineId, sessionNumber)
                if (response.isSuccessful) {
                    _exercisesBySessionState.value = Resource.Success(response.body()!!)
                } else {
                    _exercisesBySessionState.value = Resource.Error(response.message())
                }
            } catch (e: Exception) {
                _exercisesBySessionState.value = Resource.Error(e.message ?: "Error getting exercises by session")
            }
        }
    }

    fun getExercisesByDay(routineId: Long, dayOfWeek: String) {
        viewModelScope.launch {
            _exercisesByDayState.value = Resource.Loading()
            try {
                val response = RoutineExerciseService.instance.getExercisesByDay(routineId, dayOfWeek)
                if (response.isSuccessful) {
                    _exercisesByDayState.value = Resource.Success(response.body()!!)
                } else {
                    _exercisesByDayState.value = Resource.Error(response.message())
                }
            } catch (e: Exception) {
                _exercisesByDayState.value = Resource.Error(e.message ?: "Error getting exercises by day")
            }
        }
    }

    fun addMultipleExercisesToRoutine(
        routineId: Long,
        exercises: List<Pair<ExerciseResponse, AddExerciseToRoutineRequest>>
    ) {
        viewModelScope.launch {
            _addExerciseState.value = Resource.Loading()

            try {
                var allSuccessful = true
                var errorMessage = ""

                for ((exercise, request) in exercises) {
                    try {
                        val response = RoutineExerciseService.instance.addExerciseToRoutine(routineId, request)

                        if (!response.isSuccessful) {
                            allSuccessful = false
                            errorMessage = "Error agregando ejercicio ${exercise.name}: ${response.message()}"
                            Log.e(TAG, errorMessage)
                        }
                    } catch (e: Exception) {
                        allSuccessful = false
                        errorMessage = "Error agregando ejercicio ${exercise.name}: ${e.message}"
                        Log.e(TAG, errorMessage)
                    }

                    delay(200)
                }

                if (allSuccessful) {
                    _addExerciseState.value = Resource.Success(
                        RoutineExerciseResponse(
                            id = 0,
                            routineId = routineId,
                            exerciseId = 0,
                            exerciseName = "",
                            position = 0,
                            sessionNumber = null,
                            dayOfWeek = null,
                            sessionOrder = null,
                            restAfterExercise = null,
                            sets = 0,
                            targetParameters = null,
                            setsTemplate = null
                        )
                    )
                } else {
                    _addExerciseState.value = Resource.Error(errorMessage)
                }

            } catch (e: Exception) {
                _addExerciseState.value = Resource.Error(e.message ?: "Error agregando múltiples ejercicios")
            }
        }
    }

    fun reorderExercises(routineId: Long, exerciseIds: List<Long>) {
        viewModelScope.launch {
            _reorderExercisesState.value = Resource.Loading()
            try {
                val response = RoutineExerciseService.instance.reorderExercises(routineId, exerciseIds)
                if (response.isSuccessful) {
                    _reorderExercisesState.value = Resource.Success(Unit)
                } else {
                    _reorderExercisesState.value = Resource.Error(response.message())
                }
            } catch (e: Exception) {
                _reorderExercisesState.value = Resource.Error(e.message ?: "Error reordering exercises")
            }
        }
    }
}