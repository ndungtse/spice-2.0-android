package org.medtroniclabs.uhis.ui.medicalreview.familyplan.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.data.AboveFiveYearsSummaryRequest
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.data.model.FamilyPlanningCreateResponse
import org.medtroniclabs.uhis.data.model.FamilyPlanningSummaryResponse
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.model.PatientListRespModel
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.repo.MedicalReviewSummaryRepository
import org.medtroniclabs.uhis.ui.assessment.referrallogic.utils.ReferralStatus
import org.medtroniclabs.uhis.ui.medicalreview.familyplan.FamilyPlanningRepository
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

@HiltViewModel
class FamilyPlanViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val familyPlanningRepository: FamilyPlanningRepository,
    private var summaryRepository: MedicalReviewSummaryRepository,
) : ViewModel() {
    var patientId: String? = null
    var memberId: String? = null
    var isFamilyPlanSummary: Boolean = false
    val familyPlanningCreateLiveData = MutableLiveData<Resource<FamilyPlanningCreateResponse>>()
    val familyPlanningMetaLiveData = MutableLiveData<Resource<Boolean>>()
    var lastLocation: Location? = null
    var nextFollowupDate: String? = null
    val summaryDetailsLiveData = MutableLiveData<Resource<FamilyPlanningSummaryResponse>>()
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()

    fun getFamilyPlanStaticData(menuType: String) {
        viewModelScope.launch(dispatcherIO) {
            familyPlanningMetaLiveData.postLoading()
            familyPlanningMetaLiveData.postValue(
                familyPlanningRepository.getStaticMetaData(menuType),
            )
        }
    }

    fun createFamilyPlanningMR(
        details: PatientListRespModel,
        resultMap: Pair<HashMap<String, Any>, List<ChipViewItemModel>>,
        occupation: String?,
        maritalStatus: String?,
        encounterId: String?,
        notes: String?,
    ) {
        viewModelScope.launch(dispatcherIO) {
            familyPlanningCreateLiveData.postLoading()
            familyPlanningCreateLiveData.postValue(
                familyPlanningRepository.createFamilyPlanningMR(
                    details,
                    resultMap,
                    occupation,
                    maritalStatus,
                    encounterId,
                    lastLocation,
                    notes,
                ),
            )
        }
    }

    fun getFamilyPlanningSummaryDetails(request: AboveFiveYearsSummaryRequest) {
        viewModelScope.launch(dispatcherIO) {
            summaryDetailsLiveData.postLoading()
            summaryDetailsLiveData.postValue(familyPlanningRepository.getFamilyPlanningSummaryDetails(request))
        }
    }

    fun familyPlanningSummaryCreate(
        details: PatientListRespModel,
        submitEncounterId: String,
        submitPatientReferenceId: String,
    ) {
        viewModelScope.launch(dispatcherIO) {
            summaryCreateResponse.postLoading()
            val patientId = details.patientId
            val memberId = details.memberId
            val houseHoldId = details.houseHoldId
            val villageId = details.villageId

            if (patientId != null && memberId != null && villageId != null) {
                val nextVisitDate = DateUtils.convertDateTimeToDate(
                    nextFollowupDate,
                    DateUtils.DATE_ddMMyyyy,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    inUTC = true,
                )

                val response = summaryRepository.createSummarySubmit(
                    patientId = patientId,
                    patientReference = submitPatientReferenceId,
                    memberId = memberId,
                    id = submitEncounterId,
                    nextVisitDate = nextVisitDate,
                    referralTicketType = MedicalReviewTypeEnums.FAMILY_PLANNING.name,
                    assessmentName = MedicalReviewTypeEnums.FAMILY_PLANNING_REVIEW.name,
                    householdId = houseHoldId,
                    villageId = villageId,
                    patientStatus = ReferralStatus.Recovered.name,
                )
                summaryCreateResponse.postValue(response)
            }
        }
    }
}
