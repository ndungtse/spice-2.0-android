package com.medtroniclabs.spice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.internal.common.SystemCurrentTimeProvider
import com.medtroniclabs.spice.app.analytics.db.AnalyticsRepository
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils
import com.medtroniclabs.spice.appextensions.convertToLocalDateTime
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.model.FollowUpCallStatus
import com.medtroniclabs.spice.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import kotlin.math.log


@HiltViewModel
open class BaseViewModel @Inject constructor(
    @IoDispatcher open var dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    init {
        logViewModelLifecycle("Created")
    }

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository


    fun setUserJourney(userJourney: String) =
        viewModelScope.launch(dispatcherIO) {
            setUserDetails()
            analyticsRepository.insertUserJourney(userJourney)
            if(userJourney == AnalyticsDefinedParams.LOGOUT) {
                UserDetail.referenceId = UUID.randomUUID().toString()
            }
        }

    fun setAnalyticsData(
        startDate: String,
        eventType: String? = null,
        eventName: String,
        exitReason: String? = null,
        isCompleted: Boolean = true
    ) {
        viewModelScope.launch(dispatcherIO) {
            setUserDetails()
            Timber.d("userId: ${UserDetail.userId}")
            val parameter = CommonUtils.createEventParameter(
                startDate,
                eventType = eventType,
                exitReason = exitReason,
                isCompleted = isCompleted.toString()
            )
            analyticsRepository.logEvent(eventName, parameter,lastSyncDate())
        }
    }

    suspend fun setAnalyticsFollowUpData(
        id: Long,
        patientId: String?,
        callStatus: FollowUpCallStatus,
        patientStatus: String?,
        unSuccessfulReason: String?,
        startTiming: String?
    ) {
        setUserDetails()
        val parameter = CommonUtils.createFollowUpEventParameter(
            id,
            patientId,
            callStatus.name,
            patientStatus,
            unSuccessfulReason,
            startTiming
        )
        analyticsRepository.logEvent(AnalyticsDefinedParams.MyPatient, parameter,lastSyncDate())

    }

    private fun setUserDetails() {
        UserDetail.userId = SecuredPreference.getUserId().toString()
        UserDetail.role = SecuredPreference.getRole()
        UserDetail.startDateTime = CommonUtils.getCurrentDateTimeInLocalTime()
    }
    private fun lastSyncDate(): String {
        val lastSyncedAt =
            SecuredPreference.getString(SecuredPreference.EnvironmentKey.SERVER_LAST_SYNCED.name)
        return lastSyncedAt ?: "--"
    }

    override fun onCleared() {
        super.onCleared()
        logViewModelLifecycle("Cleared")
    }

    private fun logViewModelLifecycle(state: String) {
        Timber.tag("ViewModelLifecycle").d("${javaClass.simpleName} $state")
    }
}