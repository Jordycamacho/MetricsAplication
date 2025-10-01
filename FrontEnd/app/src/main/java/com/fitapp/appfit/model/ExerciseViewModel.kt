package com.fitapp.appfit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.ExerciseRepository
import com.fitapp.appfit.response.exercise.CreateExerciseRequest
import com.fitapp.appfit.response.exercise.ExerciseResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class ExerciseViewModel : ViewModel() {
    private val repository = ExerciseRepository()

    private val _exercisesState = MutableLiveData<Resource<List<ExerciseResponse>>>()
    val exercisesState: LiveData<Resource<List<ExerciseResponse>>> = _exercisesState
    private val _createExerciseState = MutableLiveData<Resource<ExerciseResponse>>()
    val createExerciseState: LiveData<Resource<ExerciseResponse>> = _createExerciseState
    private val _exerciseDetailState = MutableLiveData<Resource<ExerciseResponse>>()
    val exerciseDetailState: LiveData<Resource<ExerciseResponse>> = _exerciseDetailState

    fun getExercises() {
        _exercisesState.value = Resource.Loading()
        viewModelScope.launch {
            _exercisesState.value = repository.getExercises()
        }
    }
    fun getExercisesBySport(sportId: Long?) {
        _exercisesState.value = Resource.Loading()
        viewModelScope.launch {
            _exercisesState.value = repository.getExercisesBySport(sportId)
        }
    }
    fun getExerciseById(exerciseId: Long) {
        _exerciseDetailState.value = Resource.Loading()
        viewModelScope.launch {
            _exerciseDetailState.value = repository.getExerciseById(exerciseId)
        }
    }
    fun searchExercises(query: String, sportId: Long?) {
        _exercisesState.value = Resource.Loading()
        viewModelScope.launch {
            _exercisesState.value = repository.searchExercises(query, sportId)
        }
    }

    fun createExercise(name: String, description: String?, sportId: Long, parameterTemplates: Map<String, String>) {
        _createExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            val request = CreateExerciseRequest(
                name = name,
                description = description,
                sportId = sportId,
                parameterTemplates = parameterTemplates
            )
            _createExerciseState.value = repository.createExercise(request)
        }
    }
}