package com.medtroniclabs.spice.ui.household.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.household.HouseHoldSearchFilter
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.HouseHoldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HouseholdListViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val houseHoldRepository: HouseHoldRepository
) : ViewModel() {

    var villageListResponse = MutableLiveData<Resource<List<VillageEntity>>>()

    private val filterLiveData = MutableLiveData<HouseHoldSearchFilter>()
    val filteredHouseholdsLiveData: LiveData<List<HouseHoldEntityWithMemberCount>> =
        filterLiveData.switchMap { filter ->
            val status =
                if (filter.filterByStatus.isEmpty()) "" else filter.filterByStatus.first().name

            houseHoldRepository.getFilteredHouseholdsLiveData(
                filter.searchInput,
                filter.filterByVillage.map { it.id!! },
                status
            )
        }

    init {
        filterLiveData.value = HouseHoldSearchFilter()
    }

    fun setFilterLiveData(
        search: String? = null,
        villageFilter: List<ChipViewItemModel>? = null,
        statusFilter: List<ChipViewItemModel>? = null,
        dataRangesFilter: List<ChipViewItemModel>? = null

    ) {
        val filter = filterLiveData.value ?: HouseHoldSearchFilter()
        search?.let {
            filter.searchInput = it
        }
        villageFilter?.let {
            filter.filterByVillage = it
        }
        statusFilter?.let {
            filter.filterByStatus = it
        }
        dataRangesFilter?.let {
            filter.filterByStatus = it
        }
        filterLiveData.value = filter
    }

    fun getFilterLiveData(): LiveData<HouseHoldSearchFilter> {
        return filterLiveData
    }

    fun getAllVillagesName() {
        viewModelScope.launch(dispatcherIO) {
            houseHoldRepository.getAllVillagesName(villageListResponse)
        }
    }
}

