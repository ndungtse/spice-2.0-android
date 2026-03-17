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
import org.medtroniclabs.uhis.ncd.data.MRSummaryResponse
import org.medtroniclabs.uhis.ncd.data.MedicalReviewResponse
import org.medtroniclabs.uhis.ncd.data.NCDMRSummaryRequestResponse
import org.medtroniclabs.uhis.ncd.medicalreview.repo.NCDMedicalReviewRepository
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class NCDMedicalReviewSummaryViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository,
) : BaseViewModel(dispatcherIO) {
    val summaryResponse = MutableLiveData<Resource<MRSummaryResponse>>()
    val createNCDMRSummaryCreate = MutableLiveData<Resource<HashMap<String, Any>>>()
    var nextFollowupDate: String? = null

    fun fetchSummaryResponse(request: MedicalReviewResponse) {
        viewModelScope.launch(dispatcherIO) {
            summaryResponse.postLoading()
            summaryResponse.postValue(
                ncdMedicalReviewRepository.fetchNCDMRSummary(request),
            )
        }
    }

    fun createNCDMRSummaryCreate(
        request: NCDMRSummaryRequestResponse,
        menuId: String? = null,
    ) {
        viewModelScope.launch(dispatcherIO) {
            createNCDMRSummaryCreate.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDMedicalReviewSummaryCreation + " " + menuId,
                isCompleted = true,
            )
            createNCDMRSummaryCreate.postValue(ncdMedicalReviewRepository.createNCDMRSummaryCreate(request))
        }
    }
}
