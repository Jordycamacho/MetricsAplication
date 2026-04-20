package com.fitapp.appfit.feature.routine.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.routine.data.RoutineRepository
import com.fitapp.appfit.feature.routine.model.rutine.request.CreateRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutine.request.UpdateRoutineRequest
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineResponse
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineStatisticsResponse
import com.fitapp.appfit.feature.routine.model.rutine.response.RoutineSummaryResponse
import com.fitapp.appfit.feature.routine.util.RoutineValidation
import com.fitapp.appfit.shared.model.PageResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RoutineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RoutineRepository(application)

    private val _createRoutineState = MutableLiveData<Resource<RoutineResponse>>()
    val createRoutineState: LiveData<Resource<RoutineResponse>> = _createRoutineState

    private val _routineDetailState = MutableLiveData<Resource<RoutineResponse>>()
    val routineDetailState: LiveData<Resource<RoutineResponse>> = _routineDetailState

    private val _workoutRoutineState = MutableLiveData<Resource<RoutineResponse>>()
    val workoutRoutineState: LiveData<Resource<RoutineResponse>> = _workoutRoutineState

    private val _updateRoutineState = MutableLiveData<Resource<RoutineResponse>>()
    val updateRoutineState: LiveData<Resource<RoutineResponse>> = _updateRoutineState

    private val _deleteRoutineState = MutableLiveData<Resource<Unit>>()
    val deleteRoutineState: LiveData<Resource<Unit>> = _deleteRoutineState

    private val _routinesListState = MutableLiveData<Resource<PageResponse<RoutineSummaryResponse>>>()
    val routinesListState: LiveData<Resource<PageResponse<RoutineSummaryResponse>>> = _routinesListState

    private val _filteredRoutinesState = MutableLiveData<Resource<PageResponse<RoutineSummaryResponse>>>()
    val filteredRoutinesState: LiveData<Resource<PageResponse<RoutineSummaryResponse>>> = _filteredRoutinesState

    private val _recentRoutinesState = MutableLiveData<Resource<List<RoutineSummaryResponse>>>()
    val recentRoutinesState: LiveData<Resource<List<RoutineSummaryResponse>>> = _recentRoutinesState

    private val _activeRoutinesState = MutableLiveData<Resource<List<RoutineSummaryResponse>>>()
    val activeRoutinesState: LiveData<Resource<List<RoutineSummaryResponse>>> = _activeRoutinesState

    private val _lastUsedRoutinesState = MutableLiveData<Resource<List<RoutineSummaryResponse>>>()
    val lastUsedRoutinesState: LiveData<Resource<List<RoutineSummaryResponse>>> = _lastUsedRoutinesState

    private val _toggleActiveState = MutableLiveData<Resource<Unit>>()
    val toggleActiveState: LiveData<Resource<Unit>> = _toggleActiveState

    private val _routineStatisticsState = MutableLiveData<Resource<RoutineStatisticsResponse>>()
    val routineStatisticsState: LiveData<Resource<RoutineStatisticsResponse>> = _routineStatisticsState

    private val _generateDefaultState = MutableLiveData<Resource<Map<String, Long>>>()
    val generateDefaultState: LiveData<Resource<Map<String, Long>>> = _generateDefaultState

    private val _importRoutineState = MutableLiveData<Resource<RoutineResponse>>()
    val importRoutineState: LiveData<Resource<RoutineResponse>> = _importRoutineState

    private val _exportRoutineState = MutableLiveData<Resource<RoutineResponse>>()
    val exportRoutineState: LiveData<Resource<RoutineResponse>> = _exportRoutineState

    private val _validationErrors = MutableLiveData<Map<String, String>>()
    val validationErrors: LiveData<Map<String, String>> = _validationErrors

    private val _anyUpdateEvent = MutableLiveData<Unit>()
    val anyUpdateEvent: LiveData<Unit> = _anyUpdateEvent

    private var workoutRoutineLoaded = false

    // ── CRUD con validación ───────────────────────────────────────────────────

    fun createRoutine(
        name: String,
        description: String?,
        sportId: Long?,
        trainingDays: List<String>,
        goal: String,
        sessionsPerWeek: Int,
        version: String? = null,
        originalRoutineId: Long? = null,
        packageId: Long? = null
    ) {
        val errors = RoutineValidation.validateCreateForm(
            name, description, goal, sessionsPerWeek, trainingDays, version
        )

        if (errors.isNotEmpty()) {
            _validationErrors.value = errors
            _createRoutineState.value = Resource.Error(
                errors.values.joinToString("\n")
            )
            return
        }

        _validationErrors.value = emptyMap()
        launch(_createRoutineState) {
            val request = CreateRoutineRequest(
                name, description, sportId, trainingDays, goal, sessionsPerWeek,
                originalRoutineId, version, packageId
            )
            repository.createRoutine(request).also {
                if (it is Resource.Success) _anyUpdateEvent.postValue(Unit)
            }
        }
    }

    fun getRoutine(id: Long) = launch(_routineDetailState) {
        repository.getRoutine(id)
    }

    fun getRoutineForTraining(id: Long) {
        if (workoutRoutineLoaded && _workoutRoutineState.value is Resource.Success) {
            val current = (_workoutRoutineState.value as Resource.Success).data
            if (current?.id == id) return
        }
        workoutRoutineLoaded = true
        launch(_workoutRoutineState) { repository.getRoutineForTraining(id) }
    }

    fun updateRoutine(id: Long, request: UpdateRoutineRequest) {
        val errors = RoutineValidation.validateUpdateForm(
            request.name, request.description, request.goal,
            request.sessionsPerWeek, request.trainingDays, request.version
        )

        if (errors.isNotEmpty()) {
            _validationErrors.value = errors
            _updateRoutineState.value = Resource.Error(
                errors.values.joinToString("\n")
            )
            return
        }

        _validationErrors.value = emptyMap()
        launch(_updateRoutineState) {
            repository.updateRoutine(id, request).also {
                if (it is Resource.Success) _anyUpdateEvent.postValue(Unit)
            }
        }
    }

    fun deleteRoutine(id: Long) = launch(_deleteRoutineState) {
        repository.deleteRoutine(id).also {
            if (it is Resource.Success) _anyUpdateEvent.postValue(Unit)
        }
    }

    fun generateDefaultRoutine(type: String) {
        _generateDefaultState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.generateDefaultRoutine(type)
            _generateDefaultState.postValue(result)
            if (result is Resource.Success) {
                delay(1500)
                _routinesListState.postValue(Resource.Loading())
                val listResult = repository.getRoutines(0, 20)
                _routinesListState.postValue(listResult)
            }
        }
    }

    // ── Listados ──────────────────────────────────────────────────────────────

    fun getRoutines(page: Int = 0, size: Int = 20) = launch(_routinesListState) {
        repository.getRoutines(page, size)
    }

    fun getRoutinesWithFilters(
        sportId: Long? = null,
        name: String? = null,
        isActive: Boolean? = null,
        sortBy: String = "createdAt",
        sortDirection: String = "DESC",
        page: Int = 0,
        size: Int = 20
    ) = launch(_filteredRoutinesState) {
        repository.getRoutinesWithFilters(sportId, name, isActive, sortBy, sortDirection, page, size)
    }

    fun getRecentRoutines(limit: Int = 5) = launch(_recentRoutinesState) {
        repository.getRecentRoutines(limit)
    }

    fun getLastUsedRoutines(limit: Int = 3) = launch(_lastUsedRoutinesState) {
        repository.getLastUsedRoutines(limit)
    }

    fun getActiveRoutines() = launch(_activeRoutinesState) {
        repository.getActiveRoutines()
    }

    fun toggleRoutineActiveStatus(id: Long, active: Boolean) = launch(_toggleActiveState) {
        repository.toggleRoutineActiveStatus(id, active)
            .also { if (it is Resource.Success) _anyUpdateEvent.postValue(Unit) }
    }

    fun markRoutineAsUsed(id: Long) {
        viewModelScope.launch { repository.markRoutineAsUsed(id) }
    }

    fun getRoutineStatistics() = launch(_routineStatisticsState) {
        repository.getRoutineStatistics()
    }

    // ── V2 Methods ────────────────────────────────────────────────────────────

    fun importRoutineFromExportKey(exportKey: String) = launch(_importRoutineState) {
        repository.importRoutineFromExportKey(exportKey).also {
            if (it is Resource.Success) _anyUpdateEvent.postValue(Unit)
        }
    }

    fun getRoutineByExportKey(exportKey: String) = launch(_exportRoutineState) {
        repository.getRoutineByExportKey(exportKey)
    }

    fun registerPurchase(id: Long) {
        viewModelScope.launch {
            repository.registerPurchase(id)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun notifyAnyUpdate() {
        _anyUpdateEvent.value = Unit
    }

    fun clearValidationErrors() {
        _validationErrors.value = emptyMap()
    }

    private fun <T> launch(
        liveData: MutableLiveData<Resource<T>>,
        block: suspend () -> Resource<T>
    ) {
        liveData.value = Resource.Loading()
        viewModelScope.launch {
            liveData.postValue(block())
        }
    }
}