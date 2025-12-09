package com.fitapp.appfit.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.RoutineRepository
import com.fitapp.appfit.response.routine.CreateRoutineRequest
import com.fitapp.appfit.response.routine.RoutineResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class RoutineViewModel : ViewModel() {
    private val repository = RoutineRepository()

    // TAG para logs
    companion object {
        private const val TAG = "RoutineViewModel"
    }

    private val _createRoutineState = MutableLiveData<Resource<RoutineResponse>>()
    val createRoutineState: LiveData<Resource<RoutineResponse>> = _createRoutineState

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
}