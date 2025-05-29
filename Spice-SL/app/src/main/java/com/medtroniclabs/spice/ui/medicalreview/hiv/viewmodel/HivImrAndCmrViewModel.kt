package com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.model.HivRequestData
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.data.model.PatientEncounterResponse
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.MedicalReviewSummaryRepository
import com.medtroniclabs.spice.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HivImrAndCmrViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: HivMedicalReviewRepo,
    private val summaryRepository: MedicalReviewSummaryRepository
) : ViewModel() {
    var patientId: String? = null
    var memberId: String? = null
    var lastLocation: Location? = null
    val hivCreateResponse = MutableLiveData<Resource<PatientEncounterResponse>>()
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String,Any>>>()
    fun getSubmitCreateId(): String? {
        return hivCreateResponse.value?.data?.encounterId
    }

    fun getPatientReference(): String? {
        return hivCreateResponse.value?.data?.patientReference
    }

    fun hivCreate(request: HivRequestData) {
        viewModelScope.launch(dispatcherIO) {
            try {
                hivCreateResponse.postLoading()
                hivCreateResponse.postValue(repository.saveHIVMedicalReview(request))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createHivSummary(
        referralTicketType: String,
        memberId: String?,
        submitCreateId: String?,
        householdId: String?,
        patientReference: String?,
        nextVisitDate: String,
        patientStatus: String?,
        villageId: String?,
        patientId: String?,
        assessmentName: String,
        eMTCTStatus: String?,
        maternalOutcome: String?
    ) {
        viewModelScope.launch(dispatcherIO) {
            summaryCreateResponse.postLoading()
            if (patientId != null && memberId != null && patientStatus != null  && villageId != null && patientReference != null && submitCreateId != null) {
                val response = summaryRepository.createSummarySubmit(
                    patientId = patientId,
                    patientReference = patientReference,
                    memberId = memberId,
                    id = submitCreateId,
                    patientStatus = patientStatus,
                    nextVisitDate = nextVisitDate,
                    referralTicketType = referralTicketType,
                    assessmentName = assessmentName,
                    householdId = householdId,
                    villageId = villageId,
                    eMTCTStatus = eMTCTStatus,
                    maternalOutcome = maternalOutcome
                )
                summaryCreateResponse.postValue(response)
            }
        }
    }

    val checkRecommendationRInvestigations = MutableLiveData<Resource<HashMap<String, Boolean?>?>>()
    fun checkRecommendationRInvestigations(patientReference: String?, memberId: String?) {
        viewModelScope.launch(dispatcherIO) {
            checkRecommendationRInvestigations.postLoading()
            checkRecommendationRInvestigations.postValue(
                repository.checkRecommendationRInvestigations(
                    MotherNeonateAncRequest(patientReference = patientReference)
                )
            )
        }
    }
}