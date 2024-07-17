package com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.data.model.PatientEncounterResponse
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.MedicalReviewSummaryRepository
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.repo.MotherNeonateANCRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotherNeonateANCViewModel @Inject constructor(
    private val motherNeonateANCRepo: MotherNeonateANCRepo,
    private val summaryRepository: MedicalReviewSummaryRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    val motherNeonateMetaResponse = MutableLiveData<Resource<Boolean>>()
    val motherNeonateCreateResponse = MutableLiveData<Resource<PatientEncounterResponse>>()
    var motherNeonateAncRequest: MotherNeonateAncRequest = MotherNeonateAncRequest()
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String,Any>>>()
    var ancVisit: Int = -1
    var patientId: String? = null
    var memberId: String? = null
    var lastLocation: Location? = null


    fun getMotherNeoNateAncStaticData() {
        viewModelScope.launch(dispatcherIO) {
            motherNeonateANCRepo.getMotherNeoNateAncStaticData(motherNeonateMetaResponse)
        }
    }

    fun createMotherNeonate(encounterId: String?) {
        viewModelScope.launch(dispatcherIO) {
            try {
                motherNeonateAncRequest.apply {
                    encounter = createMedicalReviewEncounter(encounterId)
                }
                motherNeonateCreateResponse.postLoading()
                motherNeonateCreateResponse.postValue(
                    motherNeonateANCRepo.saveMotherNeonateAnc(
                        motherNeonateAncRequest
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createMedicalReviewEncounter(encounterId: String?): MedicalReviewEncounter {
        val currentTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
        return MedicalReviewEncounter(
            id = encounterId,
            patientId = this@MotherNeonateANCViewModel.patientId,
            provenance = ProvanceDto(),
            latitude = lastLocation?.latitude ?: 0.0,
            longitude = lastLocation?.longitude ?: 0.0,
            startTime = currentTime,
            endTime = currentTime,
            referred = true,
            visitNumber = ancVisit
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
        assessmentName: String
    ) {
        viewModelScope.launch(dispatcherIO) {
            summaryCreateResponse.postLoading()
            if (patientId != null && memberId != null && patientStatus != null && householdId != null && villageId != null && patientReference != null && submitCreateId != null) {
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
                    villageId = villageId
                )
                summaryCreateResponse.postValue(response)
            }
        }
    }

    fun getSubmitCreateId(): String? {
        return motherNeonateCreateResponse.value?.data?.encounterId
    }

    fun getPatientReference(): String? {
        return motherNeonateCreateResponse.value?.data?.patientReference
    }
}
