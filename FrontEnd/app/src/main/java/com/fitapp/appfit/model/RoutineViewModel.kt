package com.fitapp.appfit.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.RoutineRepository
import com.fitapp.appfit.response.routine.request.CreateRoutineRequest
import com.fitapp.appfit.response.routine.response.RoutineResponse
import com.fitapp.appfit.response.routine.response.RoutineSummaryResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class RoutineViewModel : ViewModel() {
    private val repository = RoutineRepository()

    // TAG para logs
    companion object {
        private const val TAG = "RoutineViewModel"
    }

    // Estado para crear rutina
    private val _createRoutineState = MutableLiveData<Resource<RoutineResponse>>()
    val createRoutineState: LiveData<Resource<RoutineResponse>> = _createRoutineState
    // Estado para lista de rutinas
    private val _routinesListState = MutableLiveData<Resource<List<RoutineSummaryResponse>>>()
    val routinesListState: LiveData<Resource<List<RoutineSummaryResponse>>> = _routinesListState

    // Estado para cargando
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    fun createRoutine(
        name: String,
        description: String?,
        sportId: Long?,
        trainingDays: List<String>,
        goal: String,
        sessionsPerWeek: Int,
    ) {
        _createRoutineState.value = Resource.Loading()

        Log.d(TAG, "Creando rutina:")
        Log.d(TAG, "  - Nombre: $name")
        Log.d(TAG, "  - Deporte ID: $sportId")
        Log.d(TAG, "  - Días: $trainingDays")
        Log.d(TAG, "  - Objetivo: $goal")
        Log.d(TAG, "  - Sesiones/Semana: $sessionsPerWeek")

        viewModelScope.launch {
            try {
                val request = CreateRoutineRequest(
                    name = name,
                    description = description,
                    sportId = sportId,
                    trainingDays = trainingDays,
                    goal = goal,
                    sessionsPerWeek = sessionsPerWeek
                )

                val result = repository.createRoutine(request)
                _createRoutineState.value = result

                // Log del resultado
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Rutina creada exitosamente, ID: ${result.data?.id}")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Error creando rutina: ${result.message}")
                    }
                    else -> {}
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción creando rutina: ${e.message}", e)
                _createRoutineState.value = Resource.Error("Error: ${e.message}")
            }
        }
    }

    fun getRoutines() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getRoutines()
                _routinesListState.value = result

                when (result) {
                    is Resource.Success<*> -> {
                        Log.d(TAG, "✅ Rutinas obtenidas: ${result.data?.size ?: 0}")
                    }
                    is Resource.Error<*> -> {
                        Log.e(TAG, "❌ Error obteniendo rutinas: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción obteniendo rutinas: ${e.message}", e)
                _routinesListState.value = Resource.Error("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

}