package com.fitapp.appfit.feature.marketplace.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fitapp.appfit.core.util.Resource
import com.fitapp.appfit.feature.marketplace.data.PackageRepository
import com.fitapp.appfit.feature.marketplace.data.PackageStatisticsResponse
import com.fitapp.appfit.feature.marketplace.model.request.AddPackageItemRequest
import com.fitapp.appfit.feature.marketplace.model.request.CreatePackageRequest
import com.fitapp.appfit.feature.marketplace.model.request.UpdatePackageRequest
import com.fitapp.appfit.feature.marketplace.model.response.PackageResponse
import com.fitapp.appfit.feature.marketplace.model.response.PackageSummaryResponse
import com.fitapp.appfit.feature.marketplace.model.response.PackageItemResponse
import com.fitapp.appfit.shared.model.PageResponse
import kotlinx.coroutines.launch

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PackageRepository(application)

    // ── CRUD ───────────────────────────────────────────────────────────────────

    private val _createState = MutableLiveData<Resource<PackageResponse>>()
    val createState: LiveData<Resource<PackageResponse>> = _createState

    private val _detailState = MutableLiveData<Resource<PackageResponse>>()
    val detailState: LiveData<Resource<PackageResponse>> = _detailState

    private val _updateState = MutableLiveData<Resource<PackageResponse>>()
    val updateState: LiveData<Resource<PackageResponse>> = _updateState

    private val _deleteState = MutableLiveData<Resource<Unit>>()
    val deleteState: LiveData<Resource<Unit>> = _deleteState

    // ── Búsqueda y listados ───────────────────────────────────────────────────

    private val _searchState = MutableLiveData<Resource<PageResponse<PackageSummaryResponse>>>()
    val searchState: LiveData<Resource<PageResponse<PackageSummaryResponse>>> = _searchState

    private val _userPackagesState = MutableLiveData<Resource<PageResponse<PackageSummaryResponse>>>()
    val userPackagesState: LiveData<Resource<PageResponse<PackageSummaryResponse>>> = _userPackagesState

    private val _officialPackagesState = MutableLiveData<Resource<PageResponse<PackageSummaryResponse>>>()
    val officialPackagesState: LiveData<Resource<PageResponse<PackageSummaryResponse>>> = _officialPackagesState

    private val _purchasedPackagesState = MutableLiveData<Resource<PageResponse<PackageSummaryResponse>>>()
    val purchasedPackagesState: LiveData<Resource<PageResponse<PackageSummaryResponse>>> = _purchasedPackagesState

    private val _trendingState = MutableLiveData<Resource<List<PackageSummaryResponse>>>()
    val trendingState: LiveData<Resource<List<PackageSummaryResponse>>> = _trendingState

    private val _topRatedState = MutableLiveData<Resource<List<PackageSummaryResponse>>>()
    val topRatedState: LiveData<Resource<List<PackageSummaryResponse>>> = _topRatedState

    // ── Items ──────────────────────────────────────────────────────────────────

    private val _addItemState = MutableLiveData<Resource<PackageItemResponse>>()
    val addItemState: LiveData<Resource<PackageItemResponse>> = _addItemState

    private val _removeItemState = MutableLiveData<Resource<Unit>>()
    val removeItemState: LiveData<Resource<Unit>> = _removeItemState

    private val _reorderState = MutableLiveData<Resource<PackageResponse>>()
    val reorderState: LiveData<Resource<PackageResponse>> = _reorderState

    // ── Estado ─────────────────────────────────────────────────────────────────

    private val _publishState = MutableLiveData<Resource<Unit>>()
    val publishState: LiveData<Resource<Unit>> = _publishState

    private val _deprecateState = MutableLiveData<Resource<Unit>>()
    val deprecateState: LiveData<Resource<Unit>> = _deprecateState

    private val _suspendState = MutableLiveData<Resource<Unit>>()
    val suspendState: LiveData<Resource<Unit>> = _suspendState

    private val _unsuspendState = MutableLiveData<Resource<Unit>>()
    val unsuspendState: LiveData<Resource<Unit>> = _unsuspendState

    // ── Estadísticas ───────────────────────────────────────────────────────────

    private val _statisticsState = MutableLiveData<Resource<PackageStatisticsResponse>>()
    val statisticsState: LiveData<Resource<PackageStatisticsResponse>> = _statisticsState

    // ── Interacciones ──────────────────────────────────────────────────────────

    private val _downloadState = MutableLiveData<Resource<Unit>>()
    val downloadState: LiveData<Resource<Unit>> = _downloadState

    private val _rateState = MutableLiveData<Resource<Unit>>()
    val rateState: LiveData<Resource<Unit>> = _rateState

    private val _purchaseState = MutableLiveData<Resource<Unit>>()
    val purchaseState: LiveData<Resource<Unit>> = _purchaseState

    // ── CRUD ───────────────────────────────────────────────────────────────────

    fun createPackage(request: CreatePackageRequest) = launch(_createState) {
        repository.createPackage(request)
    }

    fun getPackageById(id: Long) = launch(_detailState) {
        repository.getPackageById(id)
    }

    fun getPackageBySlug(slug: String) = launch(_detailState) {
        repository.getPackageBySlug(slug)
    }

    fun updatePackage(id: Long, request: UpdatePackageRequest) = launch(_updateState) {
        repository.updatePackage(id, request)
    }

    fun deletePackage(id: Long) = launch(_deleteState) {
        repository.deletePackage(id)
    }

    // ── Búsqueda y listados ────────────────────────────────────────────────────

    fun searchMarketplace(
        search: String? = null,
        packageType: String? = null,
        isFree: Boolean? = null,
        minRating: Double? = null,
        requiresSubscription: String? = null,
        page: Int = 0,
        size: Int = 20
    ) = launch(_searchState) {
        repository.searchMarketplace(
            search, packageType, isFree, minRating, requiresSubscription,
            page = page, size = size
        )
    }

    fun getUserPackages(userId: Long, page: Int = 0, size: Int = 20) = launch(_userPackagesState) {
        repository.getUserPackages(userId, page, size)
    }

    fun getOfficialPackages(page: Int = 0, size: Int = 20) = launch(_officialPackagesState) {
        repository.getOfficialPackages(page, size)
    }

    fun getPurchasedPackages(page: Int = 0, size: Int = 20) = launch(_purchasedPackagesState) {
        repository.getUserPurchasedPackages(page, size)
    }

    fun getTrendingPackages(limit: Int = 10) = launch(_trendingState) {
        repository.getTrendingPackages(limit)
    }

    fun getTopRatedPackages(limit: Int = 10) = launch(_topRatedState) {
        repository.getTopRatedPackages(limit)
    }

    // ── Items ──────────────────────────────────────────────────────────────────

    fun addItemToPackage(packageId: Long, request: AddPackageItemRequest) = launch(_addItemState) {
        repository.addItemToPackage(packageId, request)
    }

    fun removeItemFromPackage(packageId: Long, itemId: Long) = launch(_removeItemState) {
        repository.removeItemFromPackage(packageId, itemId)
    }

    fun reorderPackageItems(packageId: Long, itemIds: List<Long>) = launch(_reorderState) {
        repository.reorderPackageItems(packageId, itemIds)
    }

    // ── Estado ─────────────────────────────────────────────────────────────────

    fun publishPackage(id: Long) = launch(_publishState) {
        repository.publishPackage(id)
    }

    fun deprecatePackage(id: Long, reason: String? = null) = launch(_deprecateState) {
        repository.deprecatePackage(id, reason)
    }

    fun suspendPackage(id: Long, reason: String? = null) = launch(_suspendState) {
        repository.suspendPackage(id, reason)
    }

    fun unsuspendPackage(id: Long) = launch(_unsuspendState) {
        repository.unsuspendPackage(id)
    }

    // ── Estadísticas ───────────────────────────────────────────────────────────

    fun getStatistics(id: Long) = launch(_statisticsState) {
        repository.getPackageStatistics(id)
    }

    // ── Interacciones ──────────────────────────────────────────────────────────

    fun downloadPackage(id: Long) = launch(_downloadState) {
        repository.downloadPackage(id)
    }

    fun ratePackage(id: Long, rating: Double) = launch(_rateState) {
        repository.ratePackage(id, rating)
    }

    fun purchasePackage(id: Long) = launch(_purchaseState) {
        repository.purchasePackage(id)
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

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