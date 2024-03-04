package com.medtroniclabs.spice.ui.household.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.HouseHoldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HouseHoldSummaryViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val houseHoldRepository: HouseHoldRepository
) : ViewModel() {

    var houseHoldId: Long = -1L
    var isFromHouseHoldRegistration: Boolean = false
    val houseHoldDetailLiveData = MutableLiveData<Resource<HouseholdEntity>>()
    val villageDetailLiveData = MutableLiveData<Resource<VillageEntity>>()
    var selectedMemberId  = -1L
    var memberListLiveData = MutableLiveData<Resource<ArrayList<HouseholdMemberEntity>>>()

    fun getHouseHoldDetailsById() {
        if (houseHoldId == -1L)
            return
        try {
            viewModelScope.launch(dispatcherIO) {
                houseHoldDetailLiveData.postLoading()
                val householdEntity = houseHoldRepository.getHouseHoldDetailsById(houseHoldId)
                houseHoldDetailLiveData.postSuccess(householdEntity)
            }
        } catch (e: Exception) {
            houseHoldDetailLiveData.postError()
        }
    }

    fun getAllHouseHoldMemberList() {
        viewModelScope.launch(dispatcherIO) {
            try {
                memberListLiveData.postLoading()
                val memberList = houseHoldRepository.getAllHouseHoldMemberList(houseHoldId)
                memberListLiveData.postSuccess(memberList)
            } catch (e: Exception) {
                memberListLiveData.postError()
            }
        }
    }


    fun getVillageByID(villageId: Long) {
        viewModelScope.launch(dispatcherIO) {
            houseHoldRepository.getVillageByID(villageId, villageDetailLiveData)
        }
    }

}