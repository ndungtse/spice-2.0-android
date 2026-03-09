package com.medtroniclabs.spice.ui.services.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdMemberWithTb
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.household.HouseHoldFilterUiData
import com.medtroniclabs.spice.model.services.ServiceStaticFilter
import com.medtroniclabs.spice.model.services.ServicesSearchFilter
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.HouseHoldRepository
import com.medtroniclabs.spice.repo.HouseholdMemberRepository
import com.medtroniclabs.spice.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServicesViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val memberRepository: HouseholdMemberRepository,
    private val houseHoldRepository: HouseHoldRepository,
) : BaseViewModel(dispatcherIO) {
    var filterUiData = MutableLiveData<Resource<HouseHoldFilterUiData>>()

    private val filterLiveData = MutableLiveData<ServicesSearchFilter>()
    val filteredMembersLiveData: LiveData<List<HouseholdMemberWithTb>> =
        filterLiveData.switchMap { filter ->
            memberRepository.getServiceMembers(
                filter.searchInput,
                filter.filterBySs.map { it.id!! },
                filter.filterBySubVillages.map { it.id!! },
                filter.staticFilter,
            )
        }

    init {
        filterLiveData.value = ServicesSearchFilter()
    }

    fun setFilterLiveData(
        search: String? = null,
        ssFilter: List<ChipViewItemModel>? = null,
        subVillagesFilter: List<ChipViewItemModel>? = null,
        staticFilter: ServiceStaticFilter? = null,
    ) {
        val filter = filterLiveData.value ?: ServicesSearchFilter()
        search?.let {
            filter.searchInput = search
        }
        ssFilter?.let {
            filter.filterBySs = ssFilter
        }
        subVillagesFilter?.let {
            filter.filterBySubVillages = subVillagesFilter
        }
        staticFilter?.let {
            filter.staticFilter = staticFilter
        }
        filterLiveData.value = filter
    }

    fun getFilterLiveData(): LiveData<ServicesSearchFilter> = filterLiveData

    fun getFilterUiData() {
        viewModelScope.launch(dispatcherIO) {
            filterUiData.postLoading()
            filterUiData.postValue(houseHoldRepository.getHouseHoldFilterUiData(SecuredPreference.getUserId()))
        }
    }
}
