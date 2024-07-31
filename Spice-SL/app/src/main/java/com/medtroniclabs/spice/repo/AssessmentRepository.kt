package com.medtroniclabs.spice.repo

import android.location.Location
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.model.assessment.AssessmentMemberDetails
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class AssessmentRepository @Inject constructor(
    private var roomHelper: RoomHelper
) {

    suspend fun savePNCAssessment(
        second: String,
        third: String,
        memberDetail: AssessmentMemberDetails,
        referralResult: Pair<String?, ArrayList<String>>,
        lastLocation: Location?,
        otherDetails: HashMap<String, Any>?,
        childMemberId: Long,
        followUpId: Long? = null,
        childReferralResult: Pair<String?, ArrayList<String>>
    ): Resource<Pair<AssessmentEntity, AssessmentEntity>> {
        return try {
            val motherAssessmentEntity = getAssessmentEntity(
                memberDetail,
                second,
                otherDetails,
                referralResult,
                lastLocation,
                RMNCH.pnc_mother_key,
                followUpId = followUpId
            )
            motherAssessmentEntity.id = roomHelper.saveAssessment(motherAssessmentEntity)
            val childMemberDetail = roomHelper.getAssessmentMemberDetails(childMemberId)
            val childAssessmentEntity = getAssessmentEntity(
                childMemberDetail,
                third,
                if (childReferralResult.first.equals(ReferralStatus.Referred.name)) otherDetails else null,
                childReferralResult,
                lastLocation,
                RMNCH.pnc_neonate_key,
                followUpId = followUpId
            )
            childAssessmentEntity.id =  roomHelper.saveAssessment(childAssessmentEntity)
            Resource(
                state = ResourceState.SUCCESS,
                data = Pair(motherAssessmentEntity, childAssessmentEntity)
            )
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    private fun getAssessmentEntity(
        memberDetail: AssessmentMemberDetails,
        second: String,
        otherDetails: HashMap<String, Any>?,
        referralResult: Pair<String?, ArrayList<String>>,
        lastLocation: Location?,
        menuId: String,
        followUpId: Long?
    ): AssessmentEntity {

        val assessmentEntity = AssessmentEntity(
            memberId = memberDetail.memberId,
            householdId = memberDetail.householdId,
            patientId = memberDetail.patientId,
            villageId = memberDetail.villageId,
            assessmentType = menuId,
            assessmentDetails = second,
            otherDetails = if (otherDetails != null) StringConverter.convertGivenMapToString(
                otherDetails
            ) else null,
            isReferred = getReferralStatus(referralResult.first),
            referralStatus = getReferralResult(referralResult.first),
            referredReason = referralResult.second,
            followUpId = followUpId,
            latitude = lastLocation?.latitude ?: 0.0,
            longitude = lastLocation?.longitude ?: 0.0
        )

        return assessmentEntity
    }


    suspend fun saveAssessment(
        resultData: String,
        memberDetails: AssessmentMemberDetails,
        menuId: String?,
        referralResult: Pair<String?, ArrayList<String>>?,
        lastLocation: Location?,
        otherDetails: HashMap<String, Any>? = null,
        followUpId: Long? = null
    ): Resource<AssessmentEntity> {
        return try {
            val assessmentEntity = menuId?.let { menu ->
                AssessmentEntity(
                    memberId = memberDetails.memberId,
                    householdId = memberDetails.householdId,
                    patientId = memberDetails.patientId,
                    villageId = memberDetails.villageId,
                    assessmentType = menu.uppercase(Locale.getDefault()),
                    assessmentDetails = resultData,
                    otherDetails = if (otherDetails != null) StringConverter.convertGivenMapToString(
                        otherDetails
                    ) else null,
                    isReferred = getReferralStatus(referralResult?.first),
                    referralStatus = getReferralResult(referralResult?.first),
                    referredReason = referralResult?.second,
                    followUpId = followUpId,
                    latitude = lastLocation?.latitude ?: 0.0,
                    longitude = lastLocation?.longitude ?: 0.0
                )
            }
            assessmentEntity?.let {
                val id = roomHelper.saveAssessment(assessmentEntity)
                assessmentEntity.id = id
            }
            Resource(state = ResourceState.SUCCESS, data = assessmentEntity)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
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
        lastLocation: Location?
    ): Resource<String> {
        return withContext(Dispatchers.IO) {
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
                Resource(state = ResourceState.SUCCESS)
            } catch (e: Exception) {
                Resource(state = ResourceState.ERROR)
            }
        }
    }

    suspend fun updateOtherAssessmentDetails(
        pair: Pair<AssessmentEntity, AssessmentEntity>?,
        otherAssessmentDetails: HashMap<String, Any>,
        lastLocation: Location?
    ): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val motherAssessmentEntity = pair?.first
                if (motherAssessmentEntity != null) {
                    motherAssessmentEntity.otherDetails =
                        StringConverter.convertGivenMapToString(otherAssessmentDetails)
                    lastLocation?.let {
                        motherAssessmentEntity.latitude = it.latitude
                        motherAssessmentEntity.longitude = it.longitude
                    }
                    roomHelper.updateOtherAssessmentDetails(motherAssessmentEntity)
                }
                val childAssessmentEntity = pair?.second
                if (childAssessmentEntity != null && childAssessmentEntity.isReferred) {
                    childAssessmentEntity.otherDetails =
                        StringConverter.convertGivenMapToString(otherAssessmentDetails)
                    lastLocation?.let {
                        childAssessmentEntity.latitude = it.latitude
                        childAssessmentEntity.longitude = it.longitude
                    }
                    roomHelper.updateOtherAssessmentDetails(childAssessmentEntity)
                }
                Resource(state = ResourceState.SUCCESS)
            } catch (e: Exception) {
                Resource(state = ResourceState.ERROR)
            }
        }
    }

    suspend fun getSymptomListByType(
        type: String,
    ): List<SignsAndSymptomsEntity> {
        return try {
            roomHelper.getSymptomListByType(type)
        } catch (_: Exception) {
            listOf()
        }
    }

    suspend fun getFormData(
        formType: String
    ): Resource<FormResponse> {
        return try {
            val response = roomHelper.getFormData(formType)
            val formFieldsType = object : TypeToken<FormResponse>() {}.type
            val formFields: FormResponse = Gson().fromJson(response, formFieldsType)
            Resource(state = ResourceState.SUCCESS, data = formFields)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getNearestHealthFacility(): Resource<ArrayList<Map<String, Any>>> {
        val healthFacilityList = roomHelper.getNearestHealthFacility()
        val dropDownList = ArrayList<Map<String, Any>>()
        for ((_, healthFacilityEntity) in healthFacilityList.withIndex()) {
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to healthFacilityEntity.name,
                    DefinedParams.id to healthFacilityEntity.fhirId.toString()
                )
            )
        }
        return Resource(state = ResourceState.SUCCESS, data = dropDownList)
    }

    suspend fun getNearestHealthFacility(
        tag: String
    ): Resource<LocalSpinnerResponse> {
        return try {
            val response = roomHelper.getNearestHealthFacility()
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse(tag, response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getUnSyncedAssessmentCount(): Int {
        return roomHelper.getUnSyncedAssessmentCount()
    }

    suspend fun updatePregnancyAncDetail(
        patientId: String,
        visitCount: Long,
        clinicalDate: String?
    ) {
        roomHelper.updatePregnancyAncDetail(patientId, visitCount, clinicalDate)
    }


}