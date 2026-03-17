package org.medtroniclabs.uhis.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.app.analytics.model.UserDetail
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ncd.data.NCDMedicalReviewUpdateModel
import org.medtroniclabs.uhis.ncd.medicalreview.repo.NCDMedicalReviewRepository
import org.medtroniclabs.uhis.network.SingleLiveEvent
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class HrioViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository,
) : BaseViewModel(dispatcherIO) {
    val nextVisitResponse = SingleLiveEvent<Resource<HashMap<String, Any>>>()

    fun ncdUpdateNextVisitDate(request: NCDMedicalReviewUpdateModel) {
        viewModelScope.launch(dispatcherIO) {
            nextVisitResponse.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDScheduleCreation,
                isCompleted = true,
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
