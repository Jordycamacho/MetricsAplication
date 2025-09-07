package com.fitapp.appfit.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.RoutineRepository
import com.fitapp.appfit.response.routine.AddExercisesToRoutineRequest
import com.fitapp.appfit.response.routine.CreateRoutineRequest
import com.fitapp.appfit.response.routine.ExerciseRequest
import com.fitapp.appfit.response.routine.RoutineResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class RoutineViewModel : ViewModel() {
    private val repository = RoutineRepository()

    private val _createRoutineState = MutableLiveData<Resource<RoutineResponse>>()
    val createRoutineState: LiveData<Resource<RoutineResponse>> = _createRoutineState

    private val _addExercisesState = MutableLiveData<Resource<RoutineResponse>>()
    val addExercisesState: LiveData<Resource<RoutineResponse>> = _addExercisesState

    fun createRoutine(name: String, description: String?, sportId: Long?) {
        _createRoutineState.value = Resource.Loading()
        viewModelScope.launch {
            val request = CreateRoutineRequest(name, description, sportId)
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