package com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.viewmodel

import com.medtroniclabs.spice.data.model.MotherNeonatePncRequest
import com.medtroniclabs.spice.data.model.PncChild
import com.medtroniclabs.spice.data.model.PncMother
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.data.MedicalReviewSummarySubmitRequest
import com.medtroniclabs.spice.data.SummaryCreateRequest
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.model.PncSubmitResponse
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.PatientDetailRequest
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.ClinicalNotesViewModel
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.repo.MotherNeonatePNCRepo
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.medicalreview.viewmodel.GeneralExaminationViewModel
import com.medtroniclabs.spice.ui.medicalreview.viewmodel.PhysicalExaminationViewModel
import com.medtroniclabs.spice.ui.mypatients.repo.PatientRepository
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotherNeonatePNCViewModel @Inject constructor(
    private val motherNeonatePNCRepo: MotherNeonatePNCRepo,
    private val patientRepository: PatientRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    var motherLiveStatus: String? = null
    var aliveStatus: Boolean? = null
    val resultFlowHashMap = HashMap<String, Any>()
    var lastLocation: Location? = null
    var id: String? = null
    var pncVisit: Int = -1
    val motherMetaResponse = MutableLiveData<Resource<Boolean>>()
    val neonateMetaResponse = MutableLiveData<Resource<Boolean>>()
    var patientId: String? = null
    var presentingComplaints = ArrayList<ChipViewItemModel>()
    var systemicExamination = ArrayList<ChipViewItemModel>()
    var motherNeonatePncRequest: MotherNeonatePncRequest = MotherNeonatePncRequest()
    val pncSaveResponse = MutableLiveData<Resource<PncSubmitResponse>>()
    private val summaryCreateRequest: SummaryCreateRequest = SummaryCreateRequest()
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    var isNeonate = false
    var isSwipe = false
    var memberId: String? = null
    var childMemberId: String? = null
    val childDetailsLiveData = MutableLiveData<Resource<PatientListRespModel>>()

    fun getMotherPncStaticData() {
        viewModelScope.launch(dispatcherIO) {
            motherNeonatePNCRepo.getMotherPncStaticData(motherMetaResponse)
        }
    }

    fun getNeonatePncStaticData() {
        viewModelScope.launch(dispatcherIO) {
            motherNeonatePNCRepo.getNeonatePncStaticData(neonateMetaResponse)
        }
    }

    fun getChildMemberId(childPatientId: String?) {
        viewModelScope.launch(dispatcherIO) {
            childDetailsLiveData.postLoading()
            childDetailsLiveData.postValue(
                patientRepository.getPatients(
                    PatientDetailRequest(
                        patientId = childPatientId
                    )
                )
            )
        }
    }

    fun saveMotherNeonatePncData() {
        viewModelScope.launch(dispatcherIO) {
            pncSaveResponse.postLoading()
            pncSaveResponse.postValue(
                motherNeonatePNCRepo.saveMotherNeonatePncData(
                    motherNeonatePncRequest
                )
            )
        }
    }

    fun summaryCreatePncData(
        motherDetails: com.medtroniclabs.spice.data.PncMother?,
        motherPatientStatus: String?,
        motherNextVisitDate: String?,
        details: PatientListRespModel
    ) {
        val patientId = details.patientId
        val memberId = details.memberId
        val houseHoldId = details.houseHoldId
        val villageId = details.villageId
        if (patientId != null && memberId != null && villageId != null) {
            val convertedNextVisitDate = DateUtils.convertDateTimeToDate(
                motherNextVisitDate,
                DateUtils.DATE_ddMMyyyy,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                inUTC = true
            )
            val summaryCreateRequest = MedicalReviewSummarySubmitRequest(
                patientId = patientId,
                memberId = memberId,
                id = motherDetails?.id.toString(),
                patientStatus = motherPatientStatus,
                nextVisitDate = convertedNextVisitDate,
                category = MedicalReviewTypeEnums.RMNCH.name,
                encounterType = MedicalReviewTypeEnums.PNC_MEDICAL_REVIEW.name,
                householdId = houseHoldId,
                villageId = villageId,
                provenance = ProvanceDto(),
                patientReference = motherDetails?.patientReference.toString()
            )
            viewModelScope.launch(dispatcherIO) {
                summaryCreateResponse.postLoading()
                summaryCreateResponse.postValue(
                    motherNeonatePNCRepo.summaryCreatePncData(
                        summaryCreateRequest
                    )
                )
            }
        }
    }

    private fun PncMother.setCommonFields(
        clinicalNotesViewModel: ClinicalNotesViewModel,
        presentingComplaintsViewModel: PresentingComplaintsViewModel,
        systemicExaminationViewModel: GeneralExaminationViewModel
    ) {
        presentingComplaints =
            presentingComplaintsViewModel.selectedPresentingComplaints.map { it.value }
        presentingComplaintsNotes = presentingComplaintsViewModel.enteredComplaintNotes
        systemicExaminations =
            systemicExaminationViewModel.selectedSystemicExaminations.map { it.value }
        systemicExaminationsNotes = systemicExaminationViewModel.enteredExaminationNotes
        clinicalNotes = clinicalNotesViewModel.enteredClinicalNotes
    }

    fun setMotherDetailsReq(
        systemicExaminationViewModel: GeneralExaminationViewModel,
        clinicalNotesViewModel: ClinicalNotesViewModel,
        presentingComplaintsViewModel: PresentingComplaintsViewModel,
        patientViewModel: PatientDetailViewModel
    ) {
        patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
            motherNeonatePncRequest.apply {
                pncMother = PncMother(
                    id = patientViewModel.encounterId,
                    isMotherAlive = aliveStatus,
                    breastCondition = systemicExaminationViewModel.breastConditionValue,
                    breastConditionNotes = systemicExaminationViewModel.specifyCondition,
                    involutionsOfTheUterus = systemicExaminationViewModel.uterusConditionValue,
                    involutionsOfTheUterusNotes = systemicExaminationViewModel.specifyConditionUterus,
                    encounter = patientId?.let { createEncounter(
                        it,
                        patientViewModel.encounterId,
                        details,
                        true
                    ) }
                ).apply {
                    setCommonFields(
                        clinicalNotesViewModel,
                        presentingComplaintsViewModel,
                        systemicExaminationViewModel
                    )
                }
            }
        }
    }

    private fun createEncounter(
        patientId: String,
        encounterId: String?,
        details: PatientListRespModel,
        type: Boolean,
    ) = MedicalReviewEncounter(
        id = encounterId,
        patientId = patientId,
        provenance = ProvanceDto(),
        latitude = lastLocation?.latitude ?: 0.0,
        longitude = lastLocation?.longitude ?: 0.0,
        startTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ),
        endTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ),
        householdId = details.houseHoldId,
        memberId = if (type) details.memberId else childMemberId,
        referred = true,
        visitNumber = pncVisit.toInt()
    )

    fun setNeonateDetailsReq(
        physicalExaminationViewModel: PhysicalExaminationViewModel,
        presentingComplaintsViewModel: PresentingComplaintsViewModel,
        patientViewModel: PatientDetailViewModel,
        clinicalNotesViewModel: ClinicalNotesViewModel
    ) {
        patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
            motherNeonatePncRequest.apply {
                pncChild = PncChild(
                    isChildAlive = aliveStatus,
                    breastFeeding = physicalExaminationViewModel.breastFeeding,
                    exclusiveBreastFeeding = physicalExaminationViewModel.exclusiveBreastFeeding,
                    congenitalDetect = physicalExaminationViewModel.congenitalDefect,
                    encounter = patientViewModel.getChildPatientName()?.let {
                        createEncounter(
                            it,
                            patientViewModel.childEncounterId,
                            details,
                            false
                        )
                    }

                ).apply {
                    setCommonFields(
                        presentingComplaintsViewModel,
                        physicalExaminationViewModel,
                        clinicalNotesViewModel
                    )
                    setCordExamination(physicalExaminationViewModel)
                }
                saveMotherNeonatePncData()
            }
        }
    }

    private fun PncChild.setCommonFields(
        presentingComplaintsViewModel: PresentingComplaintsViewModel,
        physicalExaminationViewModel: PhysicalExaminationViewModel,
        clinicalNotesViewModel: ClinicalNotesViewModel
    ) {
        presentingComplaints =
            presentingComplaintsViewModel.selectedPresentingComplaints.map { it.value }
        presentingComplaintsNotes = presentingComplaintsViewModel.enteredComplaintNotes
        physicalExaminations =
            physicalExaminationViewModel.selectedSystemicExaminations.map { it.value }
        clinicalNotes = clinicalNotesViewModel.enteredClinicalNotes
    }

    private fun PncChild.setCordExamination(physicalExaminationViewModel: PhysicalExaminationViewModel) {
        physicalExaminationViewModel.cordExaminationMap.forEach {
            cordExamination = it.value.toString()
        }
    }
}