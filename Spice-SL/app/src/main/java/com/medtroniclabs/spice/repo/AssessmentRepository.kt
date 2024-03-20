package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import javax.inject.Inject

class AssessmentRepository @Inject constructor(
    private var roomHelper: RoomHelper
) {

    suspend fun saveAssessment(
        resultData: String,
        memberDetails: HouseholdMemberEntity,
        assessmentSaveLiveData: MutableLiveData<Resource<AssessmentEntity>>,
        menuId: String?,
        memberId: Long,
        referralResult: Pair<String?, ArrayList<String>>?
    ) {
        try {
            val assessmentEntity = menuId?.let { menu ->
                memberDetails.patientId?.let {
                    AssessmentEntity(
                        memberId = memberId,
                        householdId = memberDetails.householdId,
                        patientId = it,
                        assessmentType = menu.lowercase(),
                        assessmentDetails = resultData,
                        userId = SecuredPreference.getUserId(),
                        isReferred = getReferralStatus(referralResult?.first),
                        referralStatus = getReferralResult(referralResult?.first),
                        referredReason = referralResult?.second
                    )
                }
            }
            assessmentEntity?.let {
                roomHelper.saveAssessment(assessmentEntity)
            }
            assessmentSaveLiveData.postSuccess(assessmentEntity)
        } catch (e: Exception) {
            assessmentSaveLiveData.postError()
        }
    }

    private fun getReferralResult(result: String?): ReferralStatus {
        return when (result) {
            ReferralStatus.Referred.name -> {
                ReferralStatus.Referred
            }
            ReferralStatus.OnTreatment.name -> {
                ReferralStatus.OnTreatment
            }
            else -> {
                ReferralStatus.Recovered
            }
        }
    }

    private fun getReferralStatus(value: String?): Boolean {
        return value != null && value == ReferralStatus.Referred.name
    }

    suspend fun updateOtherAssessmentDetails(
        selectedHouseholdMemberId: Long,
        otherAssessmentDetails: HashMap<String, Any>,
        assessmentUpdateLiveData: MutableLiveData<Resource<String>>
    ) {
        try {
            val latestAssessment =
                roomHelper.getLatestAssessmentForMember(selectedHouseholdMemberId)
            if (latestAssessment != null) {
                latestAssessment.otherDetails =
                    StringConverter.convertGivenMapToString(otherAssessmentDetails)
                roomHelper.updateOtherAssessmentDetails(latestAssessment)
            }
            assessmentUpdateLiveData.postSuccess()
        } catch (e: Exception) {
            assessmentUpdateLiveData.postError()
        }
    }

    suspend fun getSymptomListByType(
        type: String,
        symptomTypeListResponse: MutableLiveData<List<SignsAndSymptomsEntity>>
    ) {
        try {
            symptomTypeListResponse.postValue(roomHelper.getSymptomListByType(type))
        } catch (_: Exception) {
            //Exception - Catch block
        }
    }
    suspend fun getFormData(
        formType: String,
        formLayoutsLiveData: MutableLiveData<Resource<FormResponse>>
    ) {
        try {
            formLayoutsLiveData.postLoading()
            val response = roomHelper.getFormData(formType)
            val formFieldsType = object : TypeToken<FormResponse>() {}.type
            val formFields: FormResponse = Gson().fromJson(response, formFieldsType)
            formLayoutsLiveData.postSuccess(formFields)
        } catch (e: Exception) {
            formLayoutsLiveData.postError()
        }
    }

    suspend fun getNearestHealthFacility(nearestFacilityLiveData: MutableLiveData<Resource<List<HealthFacilityEntity>>>) {
        val response = roomHelper.getNearestHealthFacility()
        nearestFacilityLiveData.postSuccess(response)
    }

}