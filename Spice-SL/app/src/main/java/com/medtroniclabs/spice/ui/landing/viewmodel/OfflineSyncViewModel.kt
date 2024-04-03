package com.medtroniclabs.spice.ui.landing.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.landing.OfflineSyncEntityDetail
import com.medtroniclabs.spice.offlinesync.utils.OfflineConstant.ASSESSMENTS
import com.medtroniclabs.spice.offlinesync.utils.OfflineConstant.HOUSE_HOLDS
import com.medtroniclabs.spice.offlinesync.utils.OfflineConstant.HOUSE_HOLD_MEMBERS
import com.medtroniclabs.spice.repo.AssessmentRepository
import com.medtroniclabs.spice.repo.HouseHoldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfflineSyncViewModel @Inject constructor(
    private val houseHoldRepository: HouseHoldRepository,
    private val assessmentRepository: AssessmentRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    private val entityList = mutableListOf(
        OfflineSyncEntityDetail(HOUSE_HOLDS, 0),
        OfflineSyncEntityDetail(HOUSE_HOLD_MEMBERS, 0),
        OfflineSyncEntityDetail(ASSESSMENTS, 0)
    )

    val unSyncedCountLiveData = MutableLiveData<List<OfflineSyncEntityDetail>>()

    init {
        unSyncedCountLiveData.value = entityList
        getUnSyncedCount()
    }

    private fun updateSyncedCount(index: Int, unSyncedCount: Int) {
        entityList[index].unSyncedCount = unSyncedCount
        unSyncedCountLiveData.postValue(entityList)
    }

    fun getUnSyncedCount() {
        viewModelScope.launch(dispatcherIO) {
            updateSyncedCount(0, houseHoldRepository.getUnSyncedHouseholdCount())
            updateSyncedCount(1, houseHoldRepository.getUnSyncedHouseholdMemberCount())
            updateSyncedCount(2, assessmentRepository.getUnSyncedAssessmentCount())
        }
    }
}