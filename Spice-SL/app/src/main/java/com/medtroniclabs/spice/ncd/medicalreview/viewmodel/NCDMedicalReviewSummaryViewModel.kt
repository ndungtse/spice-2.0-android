package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.MRSummaryResponse
import com.medtroniclabs.spice.ncd.data.MedicalReviewResponse
import com.medtroniclabs.spice.ncd.data.NCDMRSummaryRequestResponse
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDMedicalReviewSummaryViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository
) : ViewModel() {
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

    fun createNCDMRSummaryCreate(request: NCDMRSummaryRequestResponse) {
        viewModelScope.launch(dispatcherIO) {
            createNCDMRSummaryCreate.postLoading()
            createNCDMRSummaryCreate.postValue(ncdMedicalReviewRepository.createNCDMRSummaryCreate(request))
        }
    }
}