package com.medtroniclabs.spice.ui.medicalreview.familyplan.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.model.FamilyPlanningCreateResponse
import com.medtroniclabs.spice.data.model.FamilyPlanningSummaryResponse
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.MedicalReviewSummaryRepository
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.medicalreview.familyplan.FamilyPlanningRepository
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
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
