package com.medtroniclabs.spice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.app.analytics.db.AnalyticsRepository
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils
import com.medtroniclabs.spice.app.analytics.utils.UserDetail
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.model.FollowUpCallStatus
import com.medtroniclabs.spice.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
open class BaseViewModel @Inject constructor(
    @IoDispatcher open var dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository


    fun setUserJourney(userJourney: String) =
        viewModelScope.launch(dispatcherIO) {
            setUserId()
            analyticsRepository.insertUserJourney(userJourney)
        }

    fun setAnalyticsData(
        startDate: String,
        eventType: String? = null,
        eventName: String,
        exitReason: String? = null,
        isCompleted: Boolean = true
    ) {
        viewModelScope.launch(dispatcherIO) {
            setUserId()
            Timber.d("userId: ${UserDetail.userId}")
            val parameter = CommonUtils.createEventParameter(
                startDate,
                eventType = eventType,
                exitReason = exitReason,
                isCompleted = isCompleted.toString()
            )
            analyticsRepository.logEvent(eventName, parameter)
        }
    }

    suspend fun setAnalyticsFollowUpData(
        id: Long,
        patientId: String?,
        callStatus: FollowUpCallStatus,
        patientStatus: String?,
        unSuccessfulReason: String?
    ) {
        setUserId()
        val parameter = CommonUtils.createFollowUpEventParameter(
            id,
            patientId,
            callStatus.name,
            patientStatus,
            unSuccessfulReason
        )
        analyticsRepository.logEvent(AnalyticsDefinedParams.MyPatient, parameter)

    }

    private fun setUserId() {
        UserDetail.userId = SecuredPreference.getUserId().toString()
    }
}