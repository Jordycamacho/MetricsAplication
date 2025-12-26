// com.fitapp.appfit.model/SportViewModel.kt
package com.fitapp.appfit.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.repository.SportRepository
import com.fitapp.appfit.response.page.PageResponse
import com.fitapp.appfit.response.sport.request.SportFilterRequest
import com.fitapp.appfit.response.sport.request.SportRequest
import com.fitapp.appfit.response.sport.response.SportResponse
import com.fitapp.appfit.utils.Resource
import kotlinx.coroutines.launch

class SportViewModel : ViewModel() {
    private val repository = SportRepository()

    // Estados para listas de deportes
    private val _sportsState = MutableLiveData<Resource<List<SportResponse>>>()
    val sportsState: LiveData<Resource<List<SportResponse>>> = _sportsState

    private val _predefinedSportsState = MutableLiveData<Resource<List<SportResponse>>>()
    val predefinedSportsState: LiveData<Resource<List<SportResponse>>> = _predefinedSportsState

    private val _userSportsState = MutableLiveData<Resource<List<SportResponse>>>()
    val userSportsState: LiveData<Resource<List<SportResponse>>> = _userSportsState

    // Estados para listas paginadas
    private val _sportsPageState = MutableLiveData<Resource<PageResponse<SportResponse>>>()
    val sportsPageState: LiveData<Resource<PageResponse<SportResponse>>> = _sportsPageState

    private val _predefinedSportsPageState = MutableLiveData<Resource<PageResponse<SportResponse>>>()
    val predefinedSportsPageState: LiveData<Resource<PageResponse<SportResponse>>> = _predefinedSportsPageState

    private val _userSportsPageState = MutableLiveData<Resource<PageResponse<SportResponse>>>()
    val userSportsPageState: LiveData<Resource<PageResponse<SportResponse>>> = _userSportsPageState

    // Estado para creación y eliminación
    private val _createSportState = MutableLiveData<Resource<SportResponse>?>()
    val createSportState: LiveData<Resource<SportResponse>?> = _createSportState

    private val _deleteSportState = MutableLiveData<Resource<Unit>?>()
    val deleteSportState: LiveData<Resource<Unit>?> = _deleteSportState

    // Estado para categorías
    private val _categoriesState = MutableLiveData<Resource<List<String>>>()
    val categoriesState: LiveData<Resource<List<String>>> = _categoriesState


    // Función para obtener todos los deportes
    fun getSports() {
        _sportsState.value = Resource.Loading()
        viewModelScope.launch {
            _sportsState.value = repository.getSports()
        }
    }

    // Función para obtener deportes predefinidos
    fun getPredefinedSports() {
        _predefinedSportsState.value = Resource.Loading()
        viewModelScope.launch {
            _predefinedSportsState.value = repository.getPredefinedSports()
        }
    }

    // Función para obtener deportes del usuario
    fun getUserSports() {
        _userSportsState.value = Resource.Loading()
        viewModelScope.launch {
            _userSportsState.value = repository.getUserSports()
        }
    }

    // Función para crear un deporte personalizado
    fun createCustomSport(name: String, category: String, parameters: Map<String, String>) {
        _createSportState.value = Resource.Loading()
        viewModelScope.launch {
            val sportRequest = SportRequest(name, parameters, category, "USER_CREATED")
            _createSportState.value = repository.createCustomSport(sportRequest)
        }
    }

    // Función para eliminar un deporte personalizado
    fun deleteCustomSport(id: Long) {
        _deleteSportState.value = Resource.Loading()
        viewModelScope.launch {
            _deleteSportState.value = repository.deleteCustomSport(id)
        }
    }

    // Función para búsqueda paginada con filtros
    fun searchSports(filterRequest: SportFilterRequest) {
        _sportsPageState.value = Resource.Loading()
        viewModelScope.launch {
            _sportsPageState.value = repository.searchSports(filterRequest)
        }
    }

    // Función para búsqueda paginada de predefinidos
    fun searchPredefinedSports(filterRequest: SportFilterRequest) {
        _predefinedSportsPageState.value = Resource.Loading()
        viewModelScope.launch {
            _predefinedSportsPageState.value = repository.searchPredefinedSports(filterRequest)
        }
    }

    // Función para búsqueda paginada de personales
    fun searchUserSports(filterRequest: SportFilterRequest) {
        _userSportsPageState.value = Resource.Loading()
        viewModelScope.launch {
            _userSportsPageState.value = repository.searchUserSports(filterRequest)
        }
    }

    // Función para búsqueda rápida
    fun quickSearch(
        search: String?,
        category: String?,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "name",
        direction: String = "ASC"
    ) {
        _sportsPageState.value = Resource.Loading()
        viewModelScope.launch {
            _sportsPageState.value = repository.quickSearch(search, category, page, size, sortBy, direction)
        }
    }

    // Función para obtener categorías
    fun getCategories() {
        _categoriesState.value = Resource.Loading()
        viewModelScope.launch {
            _categoriesState.value = repository.getCategories()
        }
    }

    // Limpiar estados
    fun clearCreateState() {
        _createSportState.value = null
    }

    fun clearDeleteState() {
        _deleteSportState.value = null
    }
}