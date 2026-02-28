package com.medtroniclabs.spice.ui.household.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.db.dao.HouseholdSortOrder
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithLastActivity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.household.HouseHoldFilterUiData
import com.medtroniclabs.spice.model.household.HouseHoldSearchFilter
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.HouseHoldRepository
import com.medtroniclabs.spice.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HouseholdListViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val houseHoldRepository: HouseHoldRepository,
) : BaseViewModel(dispatcherIO) {
    var filterUiData = MutableLiveData<Resource<HouseHoldFilterUiData>>()

    private val filterLiveData = MutableLiveData<HouseHoldSearchFilter>()
    val filteredHouseholdsLiveData: LiveData<List<HouseHoldEntityWithLastActivity>> =
        filterLiveData.switchMap { filter ->
            houseHoldRepository.getFilteredHouseholdsLiveData(
                filter.searchInput,
                filter.filterBySs.map { it.id!! },
                filter.filterBySubVillages.map { it.id!! },
                filter.sortOrder,
            )
        }

    init {
        filterLiveData.value = HouseHoldSearchFilter()
    }

    fun setFilterLiveData(
        search: String? = null,
        ssFilter: List<ChipViewItemModel>? = null,
        subVillagesFilter: List<ChipViewItemModel>? = null,
        sortOrder: HouseholdSortOrder? = null,
    ) {
        val filter = filterLiveData.value ?: HouseHoldSearchFilter()
        search?.let {
            filter.searchInput = it
        }
        ssFilter?.let {
            filter.filterBySs = it
        }
        subVillagesFilter?.let {
            filter.filterBySubVillages = it
        }
        sortOrder?.let {
            filter.sortOrder = it
        }
        filterLiveData.value = filter
    }

    fun getFilterLiveData(): LiveData<HouseHoldSearchFilter> = filterLiveData

    fun getFilterUiData() {
        viewModelScope.launch(dispatcherIO) {
            filterUiData.postLoading()
            filterUiData.postValue(houseHoldRepository.getHouseHoldFilterUiData(SecuredPreference.getUserId()))
        }
    }
}
