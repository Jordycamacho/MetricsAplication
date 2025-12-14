package com.fitapp.appfit.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.RoutineRepository
import com.fitapp.appfit.response.routine.request.*
import com.fitapp.appfit.response.routine.response.*
import com.fitapp.appfit.response.page.PageResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class RoutineViewModel : ViewModel() {
    private val repository = RoutineRepository()

    companion object {
        private const val TAG = "RoutineViewModel"
    }

    // ==================== Estados LiveData ====================

    // Crear rutina
    private val _createRoutineState = MutableLiveData<Resource<RoutineResponse>>()
    val createRoutineState: LiveData<Resource<RoutineResponse>> = _createRoutineState

    // Rutina específica
    private val _routineDetailState = MutableLiveData<Resource<RoutineResponse>>()
    val routineDetailState: LiveData<Resource<RoutineResponse>> = _routineDetailState

    // Actualizar rutina
    private val _updateRoutineState = MutableLiveData<Resource<RoutineResponse>>()
    val updateRoutineState: LiveData<Resource<RoutineResponse>> = _updateRoutineState

    // Eliminar rutina
    private val _deleteRoutineState = MutableLiveData<Resource<Unit>>()
    val deleteRoutineState: LiveData<Resource<Unit>> = _deleteRoutineState

    // Agregar ejercicios
    private val _addExercisesState = MutableLiveData<Resource<RoutineResponse>>()
    val addExercisesState: LiveData<Resource<RoutineResponse>> = _addExercisesState

    // Listas de rutinas
    private val _routinesListState = MutableLiveData<Resource<PageResponse<RoutineSummaryResponse>>>()
    val routinesListState: LiveData<Resource<PageResponse<RoutineSummaryResponse>>> = _routinesListState

    private val _recentRoutinesState = MutableLiveData<Resource<List<RoutineSummaryResponse>>>()
    val recentRoutinesState: LiveData<Resource<List<RoutineSummaryResponse>>> = _recentRoutinesState

    private val _activeRoutinesState = MutableLiveData<Resource<List<RoutineSummaryResponse>>>()
    val activeRoutinesState: LiveData<Resource<List<RoutineSummaryResponse>>> = _activeRoutinesState

    // Rutinas filtradas
    private val _filteredRoutinesState = MutableLiveData<Resource<PageResponse<RoutineSummaryResponse>>>()
    val filteredRoutinesState: LiveData<Resource<PageResponse<RoutineSummaryResponse>>> = _filteredRoutinesState

    // Cambiar estado activo
    private val _toggleActiveState = MutableLiveData<Resource<Unit>>()
    val toggleActiveState: LiveData<Resource<Unit>> = _toggleActiveState

    // Estadísticas
    private val _routineStatisticsState = MutableLiveData<Resource<RoutineStatisticsResponse>>()
    val routineStatisticsState: LiveData<Resource<RoutineStatisticsResponse>> = _routineStatisticsState

    // Estado de carga general
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // ==================== Métodos públicos ====================

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

    fun getRoutine(id: Long) {
        _routineDetailState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val result = repository.getRoutine(id)
                _routineDetailState.value = result

                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Rutina obtenida: ${result.data?.id}")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Error obteniendo rutina: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción obteniendo rutina: ${e.message}", e)
                _routineDetailState.value = Resource.Error("Error: ${e.message}")
            }
        }
    }

    fun updateRoutine(id: Long, request: UpdateRoutineRequest) {
        _updateRoutineState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val result = repository.updateRoutine(id, request)
                _updateRoutineState.value = result

                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Rutina actualizada: ${result.data?.id}")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Error actualizando rutina: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción actualizando rutina: ${e.message}", e)
                _updateRoutineState.value = Resource.Error("Error: ${e.message}")
            }
        }
    }

    fun deleteRoutine(id: Long) {
        _deleteRoutineState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val result = repository.deleteRoutine(id)
                _deleteRoutineState.value = result

                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Rutina eliminada: $id")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Error eliminando rutina: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción eliminando rutina: ${e.message}", e)
                _deleteRoutineState.value = Resource.Error("Error: ${e.message}")
            }
        }
    }

    fun addExercisesToRoutine(routineId: Long, exercises: List<ExerciseRequest>) {
        _addExercisesState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val request = AddExercisesToRoutineRequest(
                    routineId = routineId,
                    exercises = exercises
                )
                val result = repository.addExercisesToRoutine(request)
                _addExercisesState.value = result

                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Ejercicios agregados a rutina: $routineId")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Error agregando ejercicios: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción agregando ejercicios: ${e.message}", e)
                _addExercisesState.value = Resource.Error("Error: ${e.message}")
            }
        }
    }

    fun getRoutines(page: Int = 0, size: Int = 10, sortBy: String = "createdAt", sortDirection: String = "DESC") {
        _isLoading.value = true
        _routinesListState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val result = repository.getRoutines(page, size, sortBy, sortDirection)
                _routinesListState.value = result

                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Rutinas obtenidas: ${result.data?.content?.size ?: 0}")
                    }
                    is Resource.Error -> {
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

    fun getRecentRoutines(limit: Int = 5) {
        _recentRoutinesState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val result = repository.getRecentRoutines(limit)
                _recentRoutinesState.value = result

                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Rutinas recientes obtenidas: ${result.data?.size ?: 0}")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Error obteniendo rutinas recientes: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción obteniendo rutinas recientes: ${e.message}", e)
                _recentRoutinesState.value = Resource.Error("Error: ${e.message}")
            }
        }
    }

    fun getActiveRoutines() {
        _activeRoutinesState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val result = repository.getActiveRoutines()
                _activeRoutinesState.value = result

                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Rutinas activas obtenidas: ${result.data?.size ?: 0}")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Error obteniendo rutinas activas: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción obteniendo rutinas activas: ${e.message}", e)
                _activeRoutinesState.value = Resource.Error("Error: ${e.message}")
            }
        }
    }

    fun getRoutinesWithFilters(
        sportId: Long? = null,
        name: String? = null,
        isActive: Boolean? = null,
        sortBy: String = "createdAt",
        sortDirection: String = "DESC",
        page: Int = 0,
        size: Int = 10
    ) {
        _filteredRoutinesState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val result = repository.getRoutinesWithFilters(
                    sportId, name, isActive, sortBy, sortDirection, page, size
                )
                _filteredRoutinesState.value = result

                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Rutinas filtradas obtenidas: ${result.data?.content?.size ?: 0}")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Error obteniendo rutinas filtradas: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción obteniendo rutinas filtradas: ${e.message}", e)
                _filteredRoutinesState.value = Resource.Error("Error: ${e.message}")
            }
        }
    }

    fun toggleRoutineActiveStatus(id: Long, active: Boolean) {
        _toggleActiveState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val result = repository.toggleRoutineActiveStatus(id, active)
                _toggleActiveState.value = result

                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Estado de rutina cambiado: $id a activo=$active")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Error cambiando estado de rutina: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción cambiando estado de rutina: ${e.message}", e)
                _toggleActiveState.value = Resource.Error("Error: ${e.message}")
            }
        }
    }

    fun getRoutineStatistics() {
        _routineStatisticsState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val result = repository.getRoutineStatistics()
                _routineStatisticsState.value = result

                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Estadísticas obtenidas")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Error obteniendo estadísticas: ${result.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Excepción obteniendo estadísticas: ${e.message}", e)
                _routineStatisticsState.value = Resource.Error("Error: ${e.message}")
            }
        }
    }
}