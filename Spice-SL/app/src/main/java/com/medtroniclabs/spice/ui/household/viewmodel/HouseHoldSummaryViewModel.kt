package com.medtroniclabs.spice.ui.household.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.data.model.HouseholdCardDetail
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.di.IoDispatcher
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
    var selectedMemberId = -1L
    var villageId: Long = -1L
    var previousCount: Int = 0

    private val houseHoldNoLiveData = MutableLiveData<Long>()
    val householdCardDetailLiveData: LiveData<HouseholdCardDetail> =
        houseHoldNoLiveData.switchMap { id ->
            houseHoldRepository.getHouseholdCardDetailLiveData(id)
        }

    val householdMembersLiveData: LiveData<List<HouseholdMemberEntity>> =
        houseHoldNoLiveData.switchMap { id ->
            houseHoldRepository.getAllHouseHoldMembersLiveData(id)
        }



    fun setHouseholdId(hhId: Long) {
        this.houseHoldId = hhId
        houseHoldNoLiveData.value = hhId
    }
}