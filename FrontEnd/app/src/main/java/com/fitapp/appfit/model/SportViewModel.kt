// com.fitapp.appfit.model/SportViewModel.kt
package com.fitapp.appfit.model

import android.util.Log
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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SportViewModel : ViewModel() {
    private val repository = SportRepository()
    private val _sportsState = MutableLiveData<Resource<List<SportResponse>>>()
    val sportsState: LiveData<Resource<List<SportResponse>>> = _sportsState

    private val _predefinedSportsState = MutableLiveData<Resource<List<SportResponse>>>()
    val predefinedSportsState: LiveData<Resource<List<SportResponse>>> = _predefinedSportsState

    private val _userSportsState = MutableLiveData<Resource<List<SportResponse>>>()
    val userSportsState: LiveData<Resource<List<SportResponse>>> = _userSportsState
    private val _sportsPageState = MutableLiveData<Resource<PageResponse<SportResponse>>>()
    val sportsPageState: LiveData<Resource<PageResponse<SportResponse>>> = _sportsPageState

    private val _predefinedSportsPageState = MutableLiveData<Resource<PageResponse<SportResponse>>>()
    val predefinedSportsPageState: LiveData<Resource<PageResponse<SportResponse>>> = _predefinedSportsPageState

    private val _userSportsPageState = MutableLiveData<Resource<PageResponse<SportResponse>>>()
    val userSportsPageState: LiveData<Resource<PageResponse<SportResponse>>> = _userSportsPageState

    private val _createSportState = MutableLiveData<Resource<SportResponse>?>()
    val createSportState: LiveData<Resource<SportResponse>?> = _createSportState

    private val _deleteSportState = MutableLiveData<Resource<Unit>?>()
    val deleteSportState: LiveData<Resource<Unit>?> = _deleteSportState

    private val _categoriesState = MutableLiveData<Resource<List<String>>>()
    val categoriesState: LiveData<Resource<List<String>>> = _categoriesState

    private val _allSportsState = MutableLiveData<Resource<List<SportResponse>>>()
    val allSportsState: LiveData<Resource<List<SportResponse>>> = _allSportsState
    fun getSports() {
        _sportsState.value = Resource.Loading()
        viewModelScope.launch {
            _sportsState.value = repository.getSports()
        }
    }

    fun getPredefinedSports() {
        _predefinedSportsState.value = Resource.Loading()
        viewModelScope.launch {
            _predefinedSportsState.value = repository.getPredefinedSports()
        }
    }

    fun getUserSports() {
        _userSportsState.value = Resource.Loading()
        viewModelScope.launch {
            _userSportsState.value = repository.getUserSports()
        }
    }

    fun createCustomSport(name: String, parameters: Map<String, String> = emptyMap()) {
        _createSportState.value = Resource.Loading()
        viewModelScope.launch {
            val sportRequest = SportRequest(name, parameters, "USER_CREATED")
            _createSportState.value = repository.createCustomSport(sportRequest)
        }
    }

    fun deleteCustomSport(id: Long) {
        _deleteSportState.value = Resource.Loading()
        viewModelScope.launch {
            _deleteSportState.value = repository.deleteCustomSport(id)
        }
    }

    fun searchSports(filterRequest: SportFilterRequest) {
        _sportsPageState.value = Resource.Loading()
        viewModelScope.launch {
            _sportsPageState.value = repository.searchSports(filterRequest)
        }
    }

    fun getAllSports() {
        _allSportsState.value = Resource.Loading()
        viewModelScope.launch {
            try {
                val predefinedResult = repository.getPredefinedSports()

                val combinedList = mutableListOf<SportResponse>()

                if (predefinedResult is Resource.Success) {
                    predefinedResult.data?.let { sports ->
                        combinedList.addAll(sports)
                    }
                }

                try {
                    val userResult = repository.getUserSports()
                    if (userResult is Resource.Success) {
                        userResult.data?.let { sports ->
                            combinedList.addAll(sports)
                        }
                    } else if (userResult is Resource.Error) {
                        Log.e("SportViewModel", "Error cargando deportes personales: ${userResult.message}")
                    }
                } catch (e: Exception) {
                    Log.e("SportViewModel", "Excepción cargando deportes personales: ${e.message}")
                }

                _allSportsState.value = Resource.Success(combinedList)
            } catch (e: Exception) {
                _allSportsState.value = Resource.Error("Error al cargar deportes: ${e.message}")
            }
        }
    }

    fun searchPredefinedSports(filterRequest: SportFilterRequest) {
        _predefinedSportsPageState.value = Resource.Loading()
        viewModelScope.launch {
            _predefinedSportsPageState.value = repository.searchPredefinedSports(filterRequest)
        }
    }

    fun searchUserSports(filterRequest: SportFilterRequest) {
        _userSportsPageState.value = Resource.Loading()
        viewModelScope.launch {
            _userSportsPageState.value = repository.searchUserSports(filterRequest)
        }
    }

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

    fun getCategories() {
        _categoriesState.value = Resource.Loading()
        viewModelScope.launch {
            _categoriesState.value = repository.getCategories()
        }
    }

    fun clearCreateState() {
        _createSportState.value = null
    }

    fun clearDeleteState() {
        _deleteSportState.value = null
    }
}