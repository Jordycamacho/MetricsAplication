package com.fitapp.appfit.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.enums.DayOfWeek
import com.fitapp.appfit.repository.RoutineRepository
import com.fitapp.appfit.response.exercise.AddExercisesToRoutineRequest
import com.fitapp.appfit.response.routine.CreateRoutineRequest
import com.fitapp.appfit.response.exercise.ExerciseRequest
import com.fitapp.appfit.response.routine.RoutineResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class RoutineViewModel : ViewModel() {
    private val repository = RoutineRepository()
    private val _createRoutineState = MutableLiveData<Resource<RoutineResponse>>()
    val createRoutineState: LiveData<Resource<RoutineResponse>> = _createRoutineState
    private val _addExercisesState = MutableLiveData<Resource<RoutineResponse>>()
    val addExercisesState: LiveData<Resource<RoutineResponse>> = _addExercisesState

    fun createRoutine(
        name: String,
        description: String?,
        sportId: Long?,
        trainingDays: List<String>,
        goal: String,
        difficultyLevel: Int,
        weeksDuration: Int,
        sessionsPerWeek: Int,
        equipmentNeeded: String
    ) {
        _createRoutineState.value = Resource.Loading()
        viewModelScope.launch {
            val request = CreateRoutineRequest(
                name = name,
                description = description,
                sportId = sportId,
                trainingDays = trainingDays,
                goal = goal,
                difficultyLevel = difficultyLevel,
                weeksDuration = weeksDuration,
                sessionsPerWeek = sessionsPerWeek,
                equipmentNeeded = equipmentNeeded
            )
            _createRoutineState.value = repository.createRoutine(request)
        }
    }

    fun addExercisesToRoutine(routineId: Long, exercises: List<ExerciseRequest>) {
        _addExercisesState.value = Resource.Loading()
        viewModelScope.launch {
            val request = AddExercisesToRoutineRequest(routineId, exercises)
            _addExercisesState.value = repository.addExercisesToRoutine(request)
        }
    }
}