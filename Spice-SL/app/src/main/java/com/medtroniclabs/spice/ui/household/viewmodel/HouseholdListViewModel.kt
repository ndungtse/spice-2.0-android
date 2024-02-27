package com.medtroniclabs.spice.ui.household.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.di.IoDispatcher
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

    //Patient list - Grid count
    var spanCount: Int = DefinedParams.span_count_1
    var villageListResponse = MutableLiveData<Resource<List<VillageEntity>>>()
    var houseHoldListLiveData = MutableLiveData<Resource<ArrayList<HouseHoldEntityWithMemberCount>>>()
    var villageFilterList : List<ChipViewItemModel>? = null
    var statusFilterList : List<ChipViewItemModel>? = null


    fun getHouseHoldList() {
        viewModelScope.launch(dispatcherIO) {
            try {
                houseHoldListLiveData.postLoading()
                val houseHoldList = houseHoldRepository.getHouseHoldList()
                houseHoldListLiveData.postSuccess(houseHoldList)
            } catch (e: Exception) {
                houseHoldListLiveData.postError()
            }
        }
    }

    fun searchByHouseholdNameOrNo(searchTerm: String) {
        viewModelScope.launch(dispatcherIO) {
            try {
                houseHoldListLiveData.postLoading()
                val houseHoldSearchList = houseHoldRepository.searchByHouseholdNameOrNo(searchTerm)
                houseHoldListLiveData.postSuccess(houseHoldSearchList)
            } catch (e: Exception) {
                houseHoldListLiveData.postError()
            }
        }
    }

    fun getAllVillagesName() {
        viewModelScope.launch(dispatcherIO) {
            houseHoldRepository.getAllVillagesName(villageListResponse)
        }
    }
}