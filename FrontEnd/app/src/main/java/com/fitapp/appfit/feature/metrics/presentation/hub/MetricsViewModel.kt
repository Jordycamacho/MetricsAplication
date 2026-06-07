package com.fitapp.appfit.feature.metrics.presentation.hub

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.feature.metrics.data.MetricsReadRepositoryImpl
import com.fitapp.appfit.feature.metrics.domain.model.MetricsOverview
import com.fitapp.appfit.feature.metrics.domain.usecase.GetMetricsOverviewUseCase
import kotlinx.coroutines.launch

class MetricsViewModel(application: Application) : AndroidViewModel(application) {

    private val overviewUseCase = GetMetricsOverviewUseCase(MetricsReadRepositoryImpl(application))

    private val _overview = MutableLiveData<MetricsOverview?>()
    val overview: LiveData<MetricsOverview?> = _overview

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun loadOverview() {
        viewModelScope.launch {
            _loading.value = true
            _overview.value = overviewUseCase()
            _loading.value = false
        }
    }
}
