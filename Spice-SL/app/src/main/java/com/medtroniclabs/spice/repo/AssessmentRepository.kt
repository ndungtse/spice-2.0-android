package com.medtroniclabs.spice.repo

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import java.util.Locale
import javax.inject.Inject

class AssessmentRepository @Inject constructor(
    private var roomHelper: RoomHelper
) {

    suspend fun saveAssessment(
        resultData: String,
        memberDetails: AssessmentMemberDetails,
        assessmentSaveLiveData: MutableLiveData<Resource<AssessmentEntity>>,
        menuId: String?,
        referralResult: Pair<String?, ArrayList<String>>?,
        lastLocation: Location?
    ) {
        try {
            val assessmentEntity = menuId?.let { menu ->
                AssessmentEntity(
                    memberId = memberDetails.memberId,
                    householdId = memberDetails.householdId,
                    patientId = memberDetails.patientId,
                    villageId = memberDetails.villageId,
                    assessmentType = menu.uppercase(Locale.getDefault()),
                    assessmentDetails = resultData,
                    isReferred = getReferralStatus(referralResult?.first),
                    referralStatus = getReferralResult(referralResult?.first),
                    referredReason = referralResult?.second,
                    latitude = lastLocation?.latitude ?: 0.0,
                    longitude = lastLocation?.longitude ?: 0.0
                )
            }
            assessmentEntity?.let {
                val id = roomHelper.saveAssessment(assessmentEntity)
                assessmentEntity.id = id
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
        assessmentEntity: AssessmentEntity?,
        otherAssessmentDetails: HashMap<String, Any>,
        assessmentUpdateLiveData: MutableLiveData<Resource<String>>,
        lastLocation: Location?
    ) {
        try {
            if (assessmentEntity != null) {
                assessmentEntity.otherDetails =
                    StringConverter.convertGivenMapToString(otherAssessmentDetails)
                lastLocation?.let {
                    assessmentEntity.latitude = it.latitude
                    assessmentEntity.longitude = it.longitude
                }
                roomHelper.updateOtherAssessmentDetails(assessmentEntity)
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

    suspend fun getNearestHealthFacility(
        facilitySpinnerLiveData: MutableLiveData<Resource<LocalSpinnerResponse>>,
        tag: String
    ) {
        try {
            facilitySpinnerLiveData.postLoading()
            val response = roomHelper.getNearestHealthFacility()
            facilitySpinnerLiveData.postValue(
                Resource(
                    ResourceState.SUCCESS,
                    LocalSpinnerResponse(tag, response)
                )
            )
        } catch (_: Exception) {
            facilitySpinnerLiveData.postError()
        }
    }

    suspend fun getUnSyncedAssessmentCount(): Int {
        return roomHelper.getUnSyncedAssessmentCount()
    }

    suspend fun updateMemberClinicalData(
        patientId: String,
        type: String,
        visitCount: Long,
        clinicalDate: String?
    ) {
        roomHelper.updateMemberClinicalData(patientId, type, visitCount, clinicalDate)
    }

}