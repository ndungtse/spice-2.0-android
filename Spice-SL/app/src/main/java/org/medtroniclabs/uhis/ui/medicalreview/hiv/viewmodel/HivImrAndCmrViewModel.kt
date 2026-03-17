package org.medtroniclabs.uhis.ui.medicalreview.hiv.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.model.HivRequestData
import org.medtroniclabs.uhis.data.model.MotherNeonateAncRequest
import org.medtroniclabs.uhis.data.model.PatientEncounterResponse
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.repo.MedicalReviewSummaryRepository
import org.medtroniclabs.uhis.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import javax.inject.Inject

@HiltViewModel
class HivImrAndCmrViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: HivMedicalReviewRepo,
    private val summaryRepository: MedicalReviewSummaryRepository,
) : ViewModel() {
    var patientId: String? = null
    var memberId: String? = null
    var lastLocation: Location? = null
    val hivCreateResponse = MutableLiveData<Resource<PatientEncounterResponse>>()
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()

    fun getSubmitCreateId(): String? = hivCreateResponse.value?.data?.encounterId

    fun getPatientReference(): String? = hivCreateResponse.value?.data?.patientReference

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
        maternalOutcome: String?,
    ) {
        viewModelScope.launch(dispatcherIO) {
            summaryCreateResponse.postLoading()
            if (patientId != null && memberId != null && patientStatus != null && villageId != null && patientReference != null && submitCreateId != null) {
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
                    maternalOutcome = maternalOutcome,
                )
                summaryCreateResponse.postValue(response)
            }
        }
    }

    val checkRecommendationRInvestigations = MutableLiveData<Resource<HashMap<String, Boolean?>?>>()

    fun checkRecommendationRInvestigations(
        patientReference: String?,
        memberId: String?,
    ) {
        viewModelScope.launch(dispatcherIO) {
            checkRecommendationRInvestigations.postLoading()
            checkRecommendationRInvestigations.postValue(
                repository.checkRecommendationRInvestigations(
                    MotherNeonateAncRequest(patientReference = patientReference, memberId = memberId),
                ),
            )
        }
    }
}
