package com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.model.HivSummaryResponse
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HivImrCmrSummaryViewModel @Inject constructor(
    private val hivMedicalReviewRepo: HivMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    var nextFollowupDate: String? = null
    val hivSummary = MutableLiveData<Resource<HivSummaryResponse>>()
    var patientStatus: String? = null
    fun fetchHivSummaryDetails(encounterId: String?, fhirId: String?) {
        viewModelScope.launch(dispatcherIO) {
            hivSummary.postLoading()
            hivSummary.postValue(
                hivMedicalReviewRepo.fetchHivSummaryDetails(
                    MotherNeonateAncRequest(id = encounterId, patientReference = fhirId)
                )
            )
        }
    }
}