package org.medtroniclabs.uhis.ui.household.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.db.dao.HouseholdSortOrder
import org.medtroniclabs.uhis.db.response.HouseHoldEntityWithLastActivity
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.model.household.HouseHoldFilterUiData
import org.medtroniclabs.uhis.model.household.HouseHoldSearchFilter
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.repo.HouseHoldRepository
import org.medtroniclabs.uhis.ui.BaseViewModel
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
