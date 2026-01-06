package com.fitapp.appfit.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.ExerciseRepository
import com.fitapp.appfit.response.exercise.request.ExerciseFilterRequest
import com.fitapp.appfit.response.exercise.request.ExerciseRequest
import com.fitapp.appfit.response.exercise.response.ExercisePageResponse
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class ExerciseViewModel : ViewModel() {
    private val repository = ExerciseRepository()

    companion object {
        private const val TAG = "ExerciseViewModel"
    }

    // Estados para listas paginadas
    private val _allExercisesState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val allExercisesState: LiveData<Resource<ExercisePageResponse>?> = _allExercisesState

    private val _myExercisesState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val myExercisesState: LiveData<Resource<ExercisePageResponse>?> = _myExercisesState

    private val _availableExercisesState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val availableExercisesState: LiveData<Resource<ExercisePageResponse>?> = _availableExercisesState

    private val _exercisesBySportState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val exercisesBySportState: LiveData<Resource<ExercisePageResponse>?> = _exercisesBySportState

    // Estados para operaciones CRUD
    private val _createExerciseState = MutableLiveData<Resource<ExerciseResponse>?>()
    val createExerciseState: LiveData<Resource<ExerciseResponse>?> = _createExerciseState

    private val _updateExerciseState = MutableLiveData<Resource<ExerciseResponse>?>()
    val updateExerciseState: LiveData<Resource<ExerciseResponse>?> = _updateExerciseState

    private val _deleteExerciseState = MutableLiveData<Resource<Void>?>()
    val deleteExerciseState: LiveData<Resource<Void>?> = _deleteExerciseState

    private val _toggleExerciseState = MutableLiveData<Resource<Void>?>()
    val toggleExerciseState: LiveData<Resource<Void>?> = _toggleExerciseState

    private val _rateExerciseState = MutableLiveData<Resource<Void>?>()
    val rateExerciseState: LiveData<Resource<Void>?> = _rateExerciseState

    private val _duplicateExerciseState = MutableLiveData<Resource<ExerciseResponse>?>()
    val duplicateExerciseState: LiveData<Resource<ExerciseResponse>?> = _duplicateExerciseState

    private val _makePublicState = MutableLiveData<Resource<ExerciseResponse>?>()
    val makePublicState: LiveData<Resource<ExerciseResponse>?> = _makePublicState

    private val _recentlyUsedState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val recentlyUsedState: LiveData<Resource<ExercisePageResponse>?> = _recentlyUsedState

    private val _mostPopularState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val mostPopularState: LiveData<Resource<ExercisePageResponse>?> = _mostPopularState

    private val _topRatedState = MutableLiveData<Resource<ExercisePageResponse>?>()
    val topRatedState: LiveData<Resource<ExercisePageResponse>?> = _topRatedState

    private val _myExerciseCountState = MutableLiveData<Resource<Long>?>()
    val myExerciseCountState: LiveData<Resource<Long>?> = _myExerciseCountState

    // Estado para un solo ejercicio (detalle)
    private val _exerciseDetailState = MutableLiveData<Resource<ExerciseResponse>?>()
    val exerciseDetailState: LiveData<Resource<ExerciseResponse>?> = _exerciseDetailState

    // Métodos para listas paginadas

    fun searchExercises(filterRequest: ExerciseFilterRequest) {
        Log.i(TAG, "searchExercises: Iniciando búsqueda general")
        _allExercisesState.value = Resource.Loading()
        viewModelScope.launch {
            _allExercisesState.value = repository.searchExercises(filterRequest)
        }
    }

    fun searchMyExercises(filterRequest: ExerciseFilterRequest) {
        Log.i(TAG, "searchMyExercises: Iniciando búsqueda de mis ejercicios")
        _myExercisesState.value = Resource.Loading()
        viewModelScope.launch {
            _myExercisesState.value = repository.searchMyExercises(filterRequest)
        }
    }

    fun searchAvailableExercises(filterRequest: ExerciseFilterRequest) {
        Log.i(TAG, "searchAvailableExercises: Iniciando búsqueda de disponibles")
        _availableExercisesState.value = Resource.Loading()
        viewModelScope.launch {
            _availableExercisesState.value = repository.searchAvailableExercises(filterRequest)
        }
    }

    fun searchExercisesBySport(sportId: Long, filterRequest: ExerciseFilterRequest) {
        Log.i(TAG, "searchExercisesBySport: Iniciando búsqueda por deporte $sportId")
        _exercisesBySportState.value = Resource.Loading()
        viewModelScope.launch {
            _exercisesBySportState.value = repository.searchExercisesBySport(sportId, filterRequest)
        }
    }

    // CRUD

    fun createExercise(exerciseRequest: ExerciseRequest) {
        Log.i(TAG, "createExercise: Creando ejercicio ${exerciseRequest.name}")
        _createExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _createExerciseState.value = repository.createExercise(exerciseRequest)
        }
    }

    fun updateExercise(id: Long, exerciseRequest: ExerciseRequest) {
        Log.i(TAG, "updateExercise: Actualizando ejercicio $id")
        _updateExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _updateExerciseState.value = repository.updateExercise(id, exerciseRequest)
        }
    }

    fun deleteExercise(id: Long) {
        Log.i(TAG, "deleteExercise: Eliminando ejercicio $id")
        _deleteExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _deleteExerciseState.value = repository.deleteExercise(id)
        }
    }

    fun toggleExerciseStatus(id: Long) {
        Log.i(TAG, "toggleExerciseStatus: Cambiando estado ejercicio $id")
        _toggleExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _toggleExerciseState.value = repository.toggleExerciseStatus(id)
        }
    }

    fun rateExercise(id: Long, rating: Double) {
        Log.i(TAG, "rateExercise: Calificando ejercicio $id con $rating")
        _rateExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _rateExerciseState.value = repository.rateExercise(id, rating)
        }
    }

    fun duplicateExercise(id: Long) {
        Log.i(TAG, "duplicateExercise: Duplicando ejercicio $id")
        _duplicateExerciseState.value = Resource.Loading()
        viewModelScope.launch {
            _duplicateExerciseState.value = repository.duplicateExercise(id)
        }
    }

    fun makeExercisePublic(id: Long) {
        Log.i(TAG, "makeExercisePublic: Haciendo público ejercicio $id")
        _makePublicState.value = Resource.Loading()
        viewModelScope.launch {
            _makePublicState.value = repository.makeExercisePublic(id)
        }
    }

    // Listas especiales

    fun getRecentlyUsedExercises(page: Int = 0, size: Int = 10) {
        Log.i(TAG, "getRecentlyUsedExercises: Obteniendo recientemente usados")
        _recentlyUsedState.value = Resource.Loading()
        viewModelScope.launch {
            _recentlyUsedState.value = repository.getRecentlyUsedExercises(page, size)
        }
    }

    fun getMostPopularExercises(page: Int = 0, size: Int = 10) {
        Log.i(TAG, "getMostPopularExercises: Obteniendo más populares")
        _mostPopularState.value = Resource.Loading()
        viewModelScope.launch {
            _mostPopularState.value = repository.getMostPopularExercises(page, size)
        }
    }

    fun getTopRatedExercises(page: Int = 0, size: Int = 10) {
        Log.i(TAG, "getTopRatedExercises: Obteniendo mejor calificados")
        _topRatedState.value = Resource.Loading()
        viewModelScope.launch {
            _topRatedState.value = repository.getTopRatedExercises(page, size)
        }
    }

    // Contador

    fun getMyExerciseCount() {
        Log.i(TAG, "getMyExerciseCount: Obteniendo mi contador")
        _myExerciseCountState.value = Resource.Loading()
        viewModelScope.launch {
            _myExerciseCountState.value = repository.getMyExerciseCount()
        }
    }

    // Detalle

    fun getExerciseById(id: Long) {
        Log.i(TAG, "getExerciseById: Obteniendo detalle ejercicio $id")
        _exerciseDetailState.value = Resource.Loading()
        viewModelScope.launch {
            _exerciseDetailState.value = repository.getExerciseById(id)
        }
    }

    fun getExerciseByIdWithRelations(id: Long) {
        Log.i(TAG, "getExerciseByIdWithRelations: Obteniendo detalle con relaciones ejercicio $id")
        _exerciseDetailState.value = Resource.Loading()
        viewModelScope.launch {
            _exerciseDetailState.value = repository.getExerciseByIdWithRelations(id)
        }
    }

    // Limpiar estados

    fun clearCreateState() {
        Log.d(TAG, "clearCreateState: Limpiando estado creación")
        _createExerciseState.value = null
    }

    fun clearUpdateState() {
        Log.d(TAG, "clearUpdateState: Limpiando estado actualización")
        _updateExerciseState.value = null
    }

    fun clearDeleteState() {
        Log.d(TAG, "clearDeleteState: Limpiando estado eliminación")
        _deleteExerciseState.value = null
    }

    fun clearToggleState() {
        Log.d(TAG, "clearToggleState: Limpiando estado toggle")
        _toggleExerciseState.value = null
    }

    fun clearRateState() {
        Log.d(TAG, "clearRateState: Limpiando estado calificación")
        _rateExerciseState.value = null
    }

    fun clearDuplicateState() {
        Log.d(TAG, "clearDuplicateState: Limpiando estado duplicación")
        _duplicateExerciseState.value = null
    }

    fun clearMakePublicState() {
        Log.d(TAG, "clearMakePublicState: Limpiando estado hacer público")
        _makePublicState.value = null
    }

    fun clearDetailState() {
        Log.d(TAG, "clearDetailState: Limpiando estado detalle")
        _exerciseDetailState.value = null
    }
}