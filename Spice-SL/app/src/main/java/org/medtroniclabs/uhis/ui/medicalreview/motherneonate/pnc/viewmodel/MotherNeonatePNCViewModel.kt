package org.medtroniclabs.uhis.ui.medicalreview.motherneonate.pnc.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.data.MedicalReviewSummarySubmitRequest
import org.medtroniclabs.uhis.data.SummaryCreateRequest
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.data.model.CreateLabourDeliveryRequest
import org.medtroniclabs.uhis.data.model.MedicalReviewEncounter
import org.medtroniclabs.uhis.data.model.MotherNeonatePncRequest
import org.medtroniclabs.uhis.data.model.PncChild
import org.medtroniclabs.uhis.data.model.PncMother
import org.medtroniclabs.uhis.data.model.PncSubmitResponse
import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.model.PatientDetailRequest
import org.medtroniclabs.uhis.model.PatientListRespModel
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.BaseViewModel
import org.medtroniclabs.uhis.ui.medicalreview.abovefiveyears.ClinicalNotesViewModel
import org.medtroniclabs.uhis.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.pnc.repo.MotherNeonatePNCRepo
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewDefinedParams
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import org.medtroniclabs.uhis.ui.medicalreview.viewmodel.GeneralExaminationViewModel
import org.medtroniclabs.uhis.ui.medicalreview.viewmodel.PhysicalExaminationViewModel
import org.medtroniclabs.uhis.ui.mypatients.repo.PatientRepository
import org.medtroniclabs.uhis.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotherNeonatePNCViewModel @Inject constructor(
    private val motherNeonatePNCRepo: MotherNeonatePNCRepo,
    private val patientRepository: PatientRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {
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
    var labourDeliveryDetails: CreateLabourDeliveryRequest? = null
    var neonateOutCome: String? = null
    var neonatePatientId: String? = null
    var isChildActive: Boolean? = false

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
                        patientId = childPatientId,
                    ),
                ),
            )
        }
    }

    fun saveMotherNeonatePncData() {
        viewModelScope.launch(dispatcherIO) {
            pncSaveResponse.postLoading()
            pncSaveResponse.postValue(
                motherNeonatePNCRepo.saveMotherNeonatePncData(
                    motherNeonatePncRequest,
                ),
            )
        }
    }

    fun summaryCreatePncData(
        motherDetails: org.medtroniclabs.uhis.data.PncMother?,
        motherPatientStatus: String?,
        motherNextVisitDate: String?,
        details: PatientListRespModel,
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
                inUTC = true,
            )
            val summaryCreateRequest = MedicalReviewSummarySubmitRequest(
                patientId = patientId,
                memberId = memberId,
                id = motherDetails?.id.toString(),
                patientStatus = motherPatientStatus,
                nextVisitDate = convertedNextVisitDate,
                category = MedicalReviewTypeEnums.RMNCH.name,
                encounterType = MedicalReviewTypeEnums.PNC_MOTHER_REVIEW.name,
                householdId = houseHoldId,
                villageId = villageId,
                provenance = ProvanceDto(),
                patientReference = motherDetails?.patientReference.toString(),
            )
            viewModelScope.launch(dispatcherIO) {
                summaryCreateResponse.postLoading()
                summaryCreateResponse.postValue(
                    motherNeonatePNCRepo.summaryCreatePncData(
                        summaryCreateRequest,
                    ),
                )
            }
        }
    }

    private fun PncMother.setCommonFields(
        clinicalNotesViewModel: ClinicalNotesViewModel,
        presentingComplaintsViewModel: PresentingComplaintsViewModel,
        systemicExaminationViewModel: GeneralExaminationViewModel,
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
        patientViewModel: PatientDetailViewModel,
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
                    encounter = patientId?.let {
                        createEncounter(
                            it,
                            patientViewModel.encounterId,
                            details,
                            true,
                        )
                    },
                    labourDTO = labourDeliveryDetails?.motherDTO?.labourDTO,
                    neonateOutcome = labourDeliveryDetails?.motherDTO?.neonateOutcome,
                ).apply {
                    setCommonFields(
                        clinicalNotesViewModel,
                        presentingComplaintsViewModel,
                        systemicExaminationViewModel,
                    )
                }
                if (labourDeliveryDetails?.neonateDTO?.neonateOutcome == MedicalReviewDefinedParams.LiveBirth &&
                    !labourDeliveryDetails?.neonateDTO?.neonateOutcome.isNullOrEmpty()
                ) {
                    child = labourDeliveryDetails?.child
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
        visitNumber = pncVisit.toInt(),
        villageId = details.villageId,
    )

    fun setNeonateDetailsReq(
        physicalExaminationViewModel: PhysicalExaminationViewModel,
        presentingComplaintsViewModel: PresentingComplaintsViewModel,
        patientViewModel: PatientDetailViewModel,
        clinicalNotesViewModel: ClinicalNotesViewModel,
    ) {
        patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
            val childPatientEncounter = patientViewModel.getChildPatientName()?.takeIf { it.isNotEmpty() }?.let { name ->
                createEncounter(name, patientViewModel.childEncounterId, details, false)
            } ?: labourDeliveryDetails?.neonateDTO?.encounter

            motherNeonatePncRequest.apply {
                pncMother?.encounter?.id = patientViewModel.encounterId
                pncMother?.id = patientViewModel.encounterId
                pncChild = PncChild(
                    isChildAlive = aliveStatus,
                    breastFeeding = physicalExaminationViewModel.breastFeeding,
                    exclusiveBreastFeeding = physicalExaminationViewModel.exclusiveBreastFeeding,
                    congenitalDetect = physicalExaminationViewModel.congenitalDefect,
                    encounter = childPatientEncounter,
                ).apply {
                    setCommonFields(
                        presentingComplaintsViewModel,
                        physicalExaminationViewModel,
                        clinicalNotesViewModel,
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
        clinicalNotesViewModel: ClinicalNotesViewModel,
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
