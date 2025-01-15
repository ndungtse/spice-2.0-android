package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.NCDMedicalReviewUpdateModel
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import com.medtroniclabs.spice.network.SingleLiveEvent
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HrioViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository
) : BaseViewModel(dispatcherIO) {
    val nextVisitResponse = SingleLiveEvent<Resource<HashMap<String, Any>>>()
    fun ncdUpdateNextVisitDate(request: NCDMedicalReviewUpdateModel) {
        viewModelScope.launch(dispatcherIO) {
            nextVisitResponse.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDScheduleCreation,
                isCompleted = true
            )
            val response = ncdMedicalReviewRepository.ncdUpdateNextVisitDate(request)
            nextVisitResponse.postValue(response)
        }
    }

    var toTriggerPatientDetails = MutableLiveData<Boolean>()
    fun toTriggerPatientDetails() {
        toTriggerPatientDetails.value = true
    }
}