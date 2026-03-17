package org.medtroniclabs.uhis.ui.landing.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.common.DateUtils.DATE_TIME_DISPLAY_FORMAT
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.data.offlinesync.model.UnAssignedHouseholdMemberDetail
import org.medtroniclabs.uhis.data.offlinesync.utils.OfflineConstant
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.model.landing.OfflineSyncEntityDetail
import org.medtroniclabs.uhis.network.utils.ConnectivityManager
import org.medtroniclabs.uhis.repo.AssessmentRepository
import org.medtroniclabs.uhis.repo.CommunityProfileRepository
import org.medtroniclabs.uhis.repo.FollowUpRepository
import org.medtroniclabs.uhis.repo.HouseHoldRepository
import org.medtroniclabs.uhis.repo.HouseholdMemberRepository
import org.medtroniclabs.uhis.repo.OfflineSyncRepository
import org.medtroniclabs.uhis.repo.RxBuddyRepository
import org.medtroniclabs.uhis.ui.BaseViewModel
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class OfflineSyncViewModel @Inject constructor(
    private val houseHoldRepository: HouseHoldRepository,
    private val assessmentRepository: AssessmentRepository,
    private val followUpRepository: FollowUpRepository,
    private val householdMemberRepository: HouseholdMemberRepository,
    private val communityProfileRepository: CommunityProfileRepository,
    private val offlineSyncRepository: OfflineSyncRepository,
    private val rxBuddyRepository: RxBuddyRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
    private val entityList = mutableListOf(
        OfflineSyncEntityDetail("Households", 0),
        OfflineSyncEntityDetail("Household Member", 0),
        OfflineSyncEntityDetail("Assessments", 0),
        OfflineSyncEntityDetail("Follow-Up", 0),
        OfflineSyncEntityDetail("Community Profile", 0),
        OfflineSyncEntityDetail("RxBuddy Register", 0),
        OfflineSyncEntityDetail("RxBuddy FollowUp", 0),
    )

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    val lastSyncedAtLiveData = MutableLiveData<String>()
    val unSyncedCountLiveData = MutableLiveData<List<OfflineSyncEntityDetail>>()
    val oldRequestIdsLiveData = MutableLiveData<Array<String>?>()
    val progressLiveData = MutableLiveData<Int>()
    val postRequestIdsLiveData = MutableLiveData<List<String>>()
    val statusLiveData = MutableLiveData<Pair<Boolean, String?>>()

    val unAssignedMembers: LiveData<List<UnAssignedHouseholdMemberDetail>>

    private var progressJob: Job? = null

    init {
        getLastSyncedAt()

        unAssignedMembers = householdMemberRepository.getUnAssignedHouseholdMember()

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
        val utcLastSyncedAt =
            SecuredPreference.getString(SecuredPreference.EnvironmentKey.SERVER_LAST_SYNCED.name)

        if (utcLastSyncedAt != null) {
            val zonedDateTime = ZonedDateTime.parse(utcLastSyncedAt)
            val currentZoneDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
            val formatter = DateTimeFormatter.ofPattern(DATE_TIME_DISPLAY_FORMAT)
            val formattedDateTime = currentZoneDateTime.format(formatter)
            lastSyncedAtLiveData.postValue(formattedDateTime)
        } else {
            lastSyncedAtLiveData.postValue("--")
        }
    }

    private fun updateSyncedCount(
        index: Int,
        unSyncedCount: Int,
    ) {
        entityList[index].unSyncedCount = unSyncedCount
        unSyncedCountLiveData.postValue(entityList)
    }

    private fun getUnSyncedCount() {
        viewModelScope.launch(dispatcherIO) {
            updateSyncedCount(0, houseHoldRepository.getUnSyncedHouseholdCount())
            updateSyncedCount(1, houseHoldRepository.getUnSyncedHouseholdMemberCount())
            updateSyncedCount(2, assessmentRepository.getUnSyncedAssessmentCount())
            updateSyncedCount(3, followUpRepository.getUnSyncedFollowUpCount())
            updateSyncedCount(4, communityProfileRepository.getUnSyncedCommunityProfileCount())
            updateSyncedCount(5, rxBuddyRepository.getUnSyncedRxBuddyRegisterCount())
            updateSyncedCount(6, rxBuddyRepository.getUnSyncedRxBuddyFollowUpCount())
        }
    }

    fun startUploadingData(minutes: Long = 3) {
        viewModelScope.launch(dispatcherIO) {
            startProgress(minutes)
            val requestIds = offlineSyncRepository.postOfflineUnSyncedChangesWithMutex(OfflineConstant.SYNC_MODE_MANUAL)
            if (requestIds != null) {
                if (requestIds.isNotEmpty()) { // Has some changes in local
                    SecuredPreference.saveStringArray(
                        SecuredPreference.EnvironmentKey.OFFLINE_SYNC_REQUEST_ID.name,
                        requestIds.toTypedArray(),
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

    fun syncCompleted(
        isSuccess: Boolean = false,
        message: String? = null,
    ) {
        getLastSyncedAt()
        progressLiveData.postValue(100)
        progressJob?.cancel()
        statusLiveData.postValue(Pair(isSuccess, message))
    }
}
