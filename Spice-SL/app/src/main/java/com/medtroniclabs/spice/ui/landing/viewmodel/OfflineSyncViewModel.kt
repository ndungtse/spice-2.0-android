package com.medtroniclabs.spice.ui.landing.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.convertToLocalDateTime
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant.ASSESSMENTS
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant.FOLLOWUPS
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant.HOUSE_HOLDS
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant.HOUSE_HOLD_MEMBERS
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.landing.OfflineSyncEntityDetail
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.repo.AssessmentRepository
import com.medtroniclabs.spice.repo.FollowUpRepository
import com.medtroniclabs.spice.repo.HouseHoldRepository
import com.medtroniclabs.spice.repo.OfflineSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfflineSyncViewModel @Inject constructor(
    private val houseHoldRepository: HouseHoldRepository,
    private val assessmentRepository: AssessmentRepository,
    private val followUpRepository: FollowUpRepository,
    private val offlineSyncRepository: OfflineSyncRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    private val entityList = mutableListOf(
        OfflineSyncEntityDetail("Households", 0),
        OfflineSyncEntityDetail("Household Member", 0),
        OfflineSyncEntityDetail("Assessments", 0),
        OfflineSyncEntityDetail("Follow-Up", 0)
    )

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    val lastSyncedAtLiveData = MutableLiveData<String>()
    val unSyncedCountLiveData = MutableLiveData<List<OfflineSyncEntityDetail>>()
    val oldRequestIdsLiveData = MutableLiveData<Array<String>?>()
    val progressLiveData = MutableLiveData<Int>()
    val postRequestIdsLiveData = MutableLiveData<List<String>>()
    val statusLiveData = MutableLiveData<Pair<Boolean, String?>>()

    private var progressJob: Job? = null

    init {
        getLastSyncedAt()

        unSyncedCountLiveData.value = entityList
        getUnSyncedCount()

        viewModelScope.launch {
            val requestIds =
                SecuredPreference.getStringArray(SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name)
            requestIds?.let {
                oldRequestIdsLiveData.postValue(it)
            }
        }
    }

    private fun getLastSyncedAt() {
        val longSyncedAt =
            SecuredPreference.getLong(SecuredPreference.EnvironmentKey.LAST_SYNCED_AT.name)
        val displayLastSyncedAt = if (longSyncedAt != 0L) longSyncedAt.convertToLocalDateTime() else "--"
        lastSyncedAtLiveData.postValue(displayLastSyncedAt)
    }

    private fun updateSyncedCount(index: Int, unSyncedCount: Int) {
        entityList[index].unSyncedCount = unSyncedCount
        unSyncedCountLiveData.postValue(entityList)
    }

    private fun getUnSyncedCount() {
        viewModelScope.launch(dispatcherIO) {
            updateSyncedCount(0, houseHoldRepository.getUnSyncedHouseholdCount())
            updateSyncedCount(1, houseHoldRepository.getUnSyncedHouseholdMemberCount())
            updateSyncedCount(2, assessmentRepository.getUnSyncedAssessmentCount())
            updateSyncedCount(3, followUpRepository.getUnSyncedFollowUpCount())
        }
    }

    fun startUploadingData(minutes: Long = 3) {
        viewModelScope.launch(dispatcherIO) {
            val requestIds = offlineSyncRepository.postOfflineUnSyncedChanges()
            if (requestIds != null) {
                startProgress(minutes)
                if (requestIds.isNotEmpty()) { // Has some changes in local
                    SecuredPreference.saveStringArray(
                        SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name,
                        requestIds.toTypedArray()
                    )
                    postRequestIdsLiveData.postValue(requestIds!!)
                } else { // no changes in local. Need to download data from server.
                    postRequestIdsLiveData.postValue(listOf())
                }
            } else { // Post local change api has failed
                syncCompleted()
            }
        }
    }

    fun startProgress(minutes: Long) {
        val initialCounterGap = (minutes * 60 * 1000) / 90
        val retryCounterGap = (minutes * 60 * 1000) / 3
        progressJob = viewModelScope.launch(dispatcherIO) {
            repeat(90) {
                progressLiveData.postValue(it)
                delay(initialCounterGap)
            }

            repeat(10) {
                progressLiveData.postValue(90 + it)
                delay(retryCounterGap)
            }
        }
    }

    fun syncCompleted(isSuccess: Boolean = false, message: String? = null) {
        getLastSyncedAt()
        progressLiveData.postValue(100)
        progressJob?.cancel()
        statusLiveData.postValue(Pair(isSuccess, message))
    }

}