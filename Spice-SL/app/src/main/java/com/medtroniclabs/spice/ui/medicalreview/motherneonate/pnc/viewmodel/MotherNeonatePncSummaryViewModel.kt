package com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.MotherNeonatePncSummaryRequest
import com.medtroniclabs.spice.data.MotherNeonatePncSummaryResponse
import com.medtroniclabs.spice.data.history.PatientStatus
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.repo.MotherNeonatePNCRepo
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotherNeonatePncSummaryViewModel @Inject constructor(
    private val motherNeonatePNCRepo: MotherNeonatePNCRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    var nextFollowupDate: String? = null
    var patientStatusMother:String? = null
    var patientStatusChild: String? = null
    private val getPncMetaForPatientStatus = MutableLiveData<String?>(null)
    var motherNeonatePncSummaryRequest = MotherNeonatePncSummaryRequest()
    val pncSummaryResponse = MutableLiveData<Resource<MotherNeonatePncSummaryResponse>>()
    var pncMotherPatientStatus:List<PatientStatus>?=null
    var pncChildPatientStatus:List<MedicalReviewMetaItems>?=null
    var motherNeonateAlive=true

    val pncMetaLiveDataForPatientStatus: LiveData<List<MedicalReviewMetaItems>> =
        getPncMetaForPatientStatus
            .switchMap { category ->
                category?.let {
                    motherNeonatePNCRepo.getExaminationsComplaintsForPnc(
                        MedicalReviewTypeEnums.patient_status.name,
                        it
                    )
                }
            }

    fun setPncReqToGetMetaForPatientStatus(category: String) {
        getPncMetaForPatientStatus.value = category
    }

    fun getPncSummaryDetails() {
        viewModelScope.launch(dispatcherIO) {
            pncSummaryResponse.postLoading()
            pncSummaryResponse.postValue(
                motherNeonatePNCRepo.getPncSummaryDetails(motherNeonatePncSummaryRequest)
            )
        }
    }
}