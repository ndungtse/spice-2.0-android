package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.MRSummaryResponse
import com.medtroniclabs.spice.ncd.data.MedicalReviewResponse
import com.medtroniclabs.spice.ncd.data.NCDMRSummaryRequestResponse
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDMedicalReviewSummaryViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository
) : BaseViewModel(dispatcherIO) {
    val summaryResponse = MutableLiveData<Resource<MRSummaryResponse>>()
    val createNCDMRSummaryCreate = MutableLiveData<Resource<HashMap<String, Any>>>()
    var nextFollowupDate: String? = null
    fun fetchSummaryResponse(request: MedicalReviewResponse) {
        viewModelScope.launch(dispatcherIO) {
            summaryResponse.postLoading()
            summaryResponse.postValue(
                ncdMedicalReviewRepository.fetchNCDMRSummary(request)
            )
        }
    }

    fun createNCDMRSummaryCreate(request: NCDMRSummaryRequestResponse, menuId: String? = null) {
        viewModelScope.launch(dispatcherIO) {
            createNCDMRSummaryCreate.postLoading()
            setAnalyticsData(
                UserDetail.startDateTime,
                eventName = AnalyticsDefinedParams.NCDMedicalReviewSummaryCreation + " " + menuId,
                isCompleted = true
            )
            createNCDMRSummaryCreate.postValue(ncdMedicalReviewRepository.createNCDMRSummaryCreate(request))
        }
    }
}