package org.medtroniclabs.uhis.ui.medicalreview.tb.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.model.PatientEncounterResponse
import org.medtroniclabs.uhis.data.model.TbMedicalReviewCreateRequest
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.repo.MedicalReviewSummaryRepository
import org.medtroniclabs.uhis.ui.medicalreview.tb.repo.TbMedicalReviewRepo
import javax.inject.Inject

@HiltViewModel
class TbViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val tbRepo: TbMedicalReviewRepo,
    private val summaryRepository: MedicalReviewSummaryRepository,
) : ViewModel() {
    var lastLocation: Location? = null
    var patientId: String? = null
    var memberId: String? = null
    val tbMetaResponse = MutableLiveData<Resource<Boolean>>()
    val tbCreateResponse = MutableLiveData<Resource<PatientEncounterResponse>>()
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()

    fun getSubmitCreateId(): String? = tbCreateResponse.value?.data?.encounterId

    fun getPatientReference(): String? = tbCreateResponse.value?.data?.patientReference

    fun getTbStaticData() {
        viewModelScope.launch(dispatcherIO) {
            tbRepo.getTbStaticData(tbMetaResponse)
        }
    }

    fun tbCreate(request: TbMedicalReviewCreateRequest) {
        viewModelScope.launch(dispatcherIO) {
            try {
                tbCreateResponse.postLoading()
                tbCreateResponse.postValue(tbRepo.saveTbMedicalReview(request))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createTbSummary(
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
        treatmentOutComes: String?,
        tbIMRCompleted: Boolean,
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
                    treatmentOutComes = treatmentOutComes,
                    tbIMRCompleted = tbIMRCompleted,
                )
                summaryCreateResponse.postValue(response)
            }
        }
    }
}
