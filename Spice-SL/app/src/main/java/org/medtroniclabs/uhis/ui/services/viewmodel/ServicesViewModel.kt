package org.medtroniclabs.uhis.ui.services.viewmodel

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
import org.medtroniclabs.uhis.data.offlinesync.model.HouseholdMemberWithTb
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.model.household.HouseHoldFilterUiData
import org.medtroniclabs.uhis.model.services.ServiceStaticFilter
import org.medtroniclabs.uhis.model.services.ServicesSearchFilter
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.repo.HouseHoldRepository
import org.medtroniclabs.uhis.repo.HouseholdMemberRepository
import org.medtroniclabs.uhis.ui.BaseViewModel
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
