package com.fitapp.appfit.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.ExerciseCategoryRepository
import com.fitapp.appfit.repository.ExerciseRepository
import com.fitapp.appfit.repository.ParameterRepository
import com.fitapp.appfit.repository.SportRepository
import com.fitapp.appfit.response.category.request.ExerciseCategoryFilterRequest
import com.fitapp.appfit.response.category.response.ExerciseCategoryResponse
import com.fitapp.appfit.response.exercise.request.ExerciseRequest
import com.fitapp.appfit.response.exercise.response.ExerciseResponse
import com.fitapp.appfit.response.parameter.request.CustomParameterFilterRequest
import com.fitapp.appfit.response.parameter.response.CustomParameterResponse
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ExerciseFormViewModel : ViewModel() {

    private val sportRepository     = SportRepository()
    private val categoryRepository  = ExerciseCategoryRepository()
    private val parameterRepository = ParameterRepository()
    private val exerciseRepository  = ExerciseRepository()

    private val _loadingForm = MutableLiveData(false)
    val loadingForm: LiveData<Boolean> = _loadingForm

    private val _sports = MutableLiveData<List<SportResponse>>(emptyList())
    val sports: LiveData<List<SportResponse>> = _sports

    private val _categories = MutableLiveData<List<ExerciseCategoryResponse>>(emptyList())
    val categories: LiveData<List<ExerciseCategoryResponse>> = _categories

    private val _parameters = MutableLiveData<List<CustomParameterResponse>>(emptyList())
    val parameters: LiveData<List<CustomParameterResponse>> = _parameters

    private val _exerciseToEdit = MutableLiveData<Resource<ExerciseResponse>?>()
    val exerciseToEdit: LiveData<Resource<ExerciseResponse>?> = _exerciseToEdit

    private val _saveResult = MutableLiveData<Resource<ExerciseResponse>?>()
    val saveResult: LiveData<Resource<ExerciseResponse>?> = _saveResult

    private val _formError = MutableLiveData<String?>()
    val formError: LiveData<String?> = _formError

    // ── CREAR: carga sports + categories en paralelo ──────────────────────────

    fun initForm() {
        _loadingForm.value = true
        viewModelScope.launch {
            try {
                val sportsDeferred     = async { fetchSports() }
                val categoriesDeferred = async { fetchCategories() }
                sportsDeferred.await()
                categoriesDeferred.await()
            } catch (e: Exception) {
                _formError.value = "Error cargando formulario: ${e.message}"
            } finally {
                _loadingForm.value = false
            }
        }
    }

    // ── EDITAR: ejercicio + sports + categories en paralelo ───────────────────

    fun initEditForm(exerciseId: Long) {
        _loadingForm.value = true
        _exerciseToEdit.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val exerciseDeferred   = async { exerciseRepository.getExerciseById(exerciseId) }
                val sportsDeferred     = async { fetchSports() }
                val categoriesDeferred = async { fetchCategories() }

                val exerciseResult = exerciseDeferred.await()
                sportsDeferred.await()
                categoriesDeferred.await()

                _exerciseToEdit.value = exerciseResult

                // Cargar parámetros del primer deporte del ejercicio
                if (exerciseResult is Resource.Success) {
                    exerciseResult.data?.sportIds()?.firstOrNull()?.let { sportId ->
                        loadParametersForSport(sportId)
                    }
                }
            } catch (e: Exception) {
                _exerciseToEdit.value = Resource.Error("Error: ${e.message}")
            } finally {
                _loadingForm.value = false
            }
        }
    }

    // ── Parámetros por deporte ────────────────────────────────────────────────

    fun loadParametersForSport(sportId: Long) {
        viewModelScope.launch {
            try {
                val filter = CustomParameterFilterRequest(sportId = sportId, isActive = true)
                val result = parameterRepository.searchAvailableParameters(sportId, filter)
                _parameters.value = if (result is Resource.Success)
                    result.data?.content ?: emptyList()
                else emptyList()
            } catch (e: Exception) {
                _parameters.value = emptyList()
            }
        }
    }

    fun clearParameters() { _parameters.value = emptyList() }

    // ── Guardar ───────────────────────────────────────────────────────────────

    fun createExercise(request: ExerciseRequest) {
        _saveResult.value = Resource.Loading()
        viewModelScope.launch { _saveResult.value = exerciseRepository.createExercise(request) }
    }

    fun updateExercise(id: Long, request: ExerciseRequest) {
        _saveResult.value = Resource.Loading()
        viewModelScope.launch { _saveResult.value = exerciseRepository.updateExercise(id, request) }
    }

    fun clearSaveResult() { _saveResult.value = null }
    fun clearFormError()  { _formError.value = null }

    // ── Privados ──────────────────────────────────────────────────────────────

    private suspend fun fetchSports() {
        // SportRepository.getSports() devuelve Resource<List<SportResponse>>
        val result = sportRepository.getSports()
        _sports.value = if (result is Resource.Success) result.data ?: emptyList() else emptyList()
    }

    private suspend fun fetchCategories() {
        // ExerciseCategoryRepository.searchCategories() necesita un FilterRequest
        val filter = ExerciseCategoryFilterRequest(
            page = 0,
            size = 200,   // traemos todas de una vez para el selector
            isActive = true
        )
        val result = categoryRepository.searchCategories(filter)
        _categories.value = if (result is Resource.Success)
            result.data?.content ?: emptyList()
        else emptyList()
    }
}