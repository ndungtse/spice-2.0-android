package org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.data.model.MedicalReviewEncounter
import org.medtroniclabs.uhis.data.model.MotherNeonateAncRequest
import org.medtroniclabs.uhis.data.model.PatientEncounterResponse
import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.repo.MedicalReviewSummaryRepository
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.repo.MotherNeonateANCRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotherNeonateANCViewModel @Inject constructor(
    private val motherNeonateANCRepo: MotherNeonateANCRepo,
    private val summaryRepository: MedicalReviewSummaryRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
    val motherNeonateMetaResponse = MutableLiveData<Resource<Boolean>>()
    val motherNeonateCreateResponse = MutableLiveData<Resource<PatientEncounterResponse>>()
    var motherNeonateAncRequest: MotherNeonateAncRequest = MotherNeonateAncRequest()
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    var ancVisit: Int = -1
    var patientId: String? = null
    var memberId: String? = null
    var lastLocation: Location? = null

    fun getMotherNeoNateAncStaticData() {
        viewModelScope.launch(dispatcherIO) {
            motherNeonateANCRepo.getMotherNeoNateAncStaticData(motherNeonateMetaResponse)
        }
    }

    fun createMotherNeonate(
        encounterId: String?,
        patientHouseholdId: String?,
        memberId: String?,
        villageId: String?,
    ) {
        viewModelScope.launch(dispatcherIO) {
            try {
                motherNeonateAncRequest.apply {
                    encounter =
                        createMedicalReviewEncounter(encounterId, patientHouseholdId, memberId, villageId)
                }
                motherNeonateCreateResponse.postLoading()
                motherNeonateCreateResponse.postValue(
                    motherNeonateANCRepo.saveMotherNeonateAnc(
                        motherNeonateAncRequest,
                    ),
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createMedicalReviewEncounter(
        encounterId: String?,
        patientHouseholdId: String?,
        memberId: String?,
        villageId: String?,
    ): MedicalReviewEncounter {
        val currentTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
        return MedicalReviewEncounter(
            id = encounterId,
            patientId = this@MotherNeonateANCViewModel.patientId,
            provenance = ProvanceDto(),
            memberId = memberId,
            latitude = lastLocation?.latitude ?: 0.0,
            longitude = lastLocation?.longitude ?: 0.0,
            startTime = currentTime,
            endTime = currentTime,
            householdId = patientHouseholdId,
            referred = true,
            visitNumber = ancVisit,
            villageId = villageId,
        )
    }

    fun motherNeonateSummaryCreate(
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
                )
                summaryCreateResponse.postValue(response)
            }
        }
    }

    fun getSubmitCreateId(): String? = motherNeonateCreateResponse.value?.data?.encounterId

    fun getPatientReference(): String? = motherNeonateCreateResponse.value?.data?.patientReference
}
