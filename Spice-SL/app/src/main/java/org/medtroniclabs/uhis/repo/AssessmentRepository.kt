package org.medtroniclabs.uhis.repo

import android.location.Location
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.common.StringConverter
import org.medtroniclabs.uhis.data.LocalSpinnerResponse
import org.medtroniclabs.uhis.db.entity.AssessmentEntity
import org.medtroniclabs.uhis.db.entity.HealthFacilityEntity
import org.medtroniclabs.uhis.db.entity.MedicalComplianceEntity
import org.medtroniclabs.uhis.db.entity.MentalHealthEntity
import org.medtroniclabs.uhis.db.entity.SignsAndSymptomsEntity
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.formgeneration.model.FormLayout
import org.medtroniclabs.uhis.formgeneration.model.FormResponse
import org.medtroniclabs.uhis.model.assessment.AssessmentMemberDetails
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.TBContactTracing
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams.TBScreening
import org.medtroniclabs.uhis.ui.assessment.AssessmentNCDEntity
import org.medtroniclabs.uhis.ui.assessment.referrallogic.utils.ReferralStatus
import org.medtroniclabs.uhis.ui.assessment.rmnch.RMNCH
import java.util.Locale
import javax.inject.Inject

class AssessmentRepository @Inject constructor(
    private var roomHelper: RoomHelper,
    private var apiHelper: ApiHelper,
) {
    suspend fun savePNCAssessment(
        formLayouts: List<FormLayout>?,
        motherAssessmentString: String,
        memberDetail: AssessmentMemberDetails,
        referralResult: Pair<String?, ArrayList<String>>,
        lastLocation: Location?,
        otherDetails: HashMap<String, Any>?,
        followUpId: Long? = null,
    ): Resource<Pair<List<FormLayout>?, AssessmentEntity>> =
        try {
            val motherAsstDetail = JsonParser.parseString(motherAssessmentString)

            val motherAssessmentEntity = getAssessmentEntity(
                memberDetail,
                motherAsstDetail.toString(),
                otherDetails,
                referralResult,
                lastLocation,
                RMNCH.pnc_mother_key,
                followUpId = followUpId,
            )
            motherAssessmentEntity.id = roomHelper.saveAssessment(motherAssessmentEntity)

            Resource(
                state = ResourceState.SUCCESS,
                data = Pair(formLayouts, motherAssessmentEntity),
            )
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    private fun getAssessmentEntity(
        memberDetail: AssessmentMemberDetails,
        second: String,
        otherDetails: HashMap<String, Any>?,
        referralResult: Pair<String?, ArrayList<String>>,
        lastLocation: Location?,
        menuId: String,
        followUpId: Long?,
    ): AssessmentEntity {
        val assessmentEntity = AssessmentEntity(
            householdMemberLocalId = memberDetail.id,
            memberId = memberDetail.memberId,
            householdId = memberDetail.householdId,
            patientId = memberDetail.patientId,
            villageId = memberDetail.villageId,
            assessmentType = menuId,
            assessmentDetails = second,
            otherDetails = if (otherDetails != null) {
                StringConverter.convertGivenMapToString(
                    otherDetails,
                )
            } else {
                null
            },
            isReferred = getReferralStatus(referralResult.first),
            referralStatus = getReferralResult(referralResult.first),
            referredReason = referralResult.second,
            followUpId = followUpId,
            latitude = lastLocation?.latitude ?: 0.0,
            longitude = lastLocation?.longitude ?: 0.0,
        )

        return assessmentEntity
    }

    suspend fun saveAssessment(
        resultData: String,
        memberDetails: AssessmentMemberDetails,
        menuId: String?,
        referralResult: Pair<String?, ArrayList<String>>?,
        otherDetails: HashMap<String, Any>? = null,
        followUpId: Long? = null,
    ): Resource<AssessmentEntity> {
        val latitude = SecuredPreference.getDouble(
            SecuredPreference.EnvironmentKey.CURRENT_LATITUDE.name,
            0.0,
        )
        val longitude = SecuredPreference.getDouble(
            SecuredPreference.EnvironmentKey.CURRENT_LONGITUDE.name,
            0.0,
        )
        return try {
            val assessmentEntity = menuId?.let { menu ->
                AssessmentEntity(
                    householdMemberLocalId = memberDetails.id,
                    memberId = memberDetails.memberId,
                    householdId = memberDetails.householdId,
                    patientId = memberDetails.patientId,
                    villageId = memberDetails.villageId,
                    assessmentType = menu.uppercase(Locale.getDefault()),
                    assessmentDetails = resultData,
                    otherDetails = if (otherDetails != null) {
                        StringConverter.convertGivenMapToString(
                            otherDetails,
                        )
                    } else {
                        null
                    },
                    isReferred = getReferralStatus(referralResult?.first),
                    referralStatus = getReferralResult(referralResult?.first),
                    referredReason = referralResult?.second,
                    followUpId = followUpId,
                    latitude = latitude,
                    longitude = longitude,
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

    suspend fun saveCallResult(assessmentEntity: AssessmentEntity): Resource<AssessmentEntity> {
        val id = roomHelper.saveAssessment(assessmentEntity)
        assessmentEntity.id = id
        return Resource(ResourceState.SUCCESS, assessmentEntity)
    }

    suspend fun getAssessmentById(assessmentId: Long): Resource<AssessmentEntity> {
        val data = roomHelper.getAssessment(assessmentId)
        return Resource(ResourceState.SUCCESS, data)
    }

    private fun getReferralResult(result: String?): ReferralStatus =
        when (result) {
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

    private fun getReferralStatus(value: String?): Boolean = value != null && value == ReferralStatus.Referred.name

    suspend fun updateOtherAssessmentDetails(
        assessmentEntity: AssessmentEntity?,
        otherAssessmentDetails: HashMap<String, Any>,
        lastLocation: Location?,
    ): Resource<String> =
        withContext(Dispatchers.IO) {
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

    suspend fun getSymptomListByType(type: String): List<SignsAndSymptomsEntity> =
        try {
            roomHelper.getSymptomListByType(type)
        } catch (_: Exception) {
            listOf()
        }

    suspend fun getFormData(
        formType: String,
        tbType: String? = null,
    ): Resource<FormResponse> =
        try {
            val response = roomHelper.getFormData(formType)
            val formFieldsType = object : TypeToken<FormResponse>() {}.type
            val formFields: FormResponse = Gson().fromJson(response, formFieldsType)
            if (tbType != null) {
                if (tbType == TBContactTracing) {
                    val filteredList =
                        formFields.formLayout.filter {
                            it.id == TBContactTracing ||
                                it.family == TBContactTracing ||
                                it.id == TBScreening ||
                                it.family == TBScreening
                        }
                    formFields.formLayout = filteredList
                } else {
                    val filteredList =
                        formFields.formLayout.filter { it.id == tbType || it.family == tbType }
                    formFields.formLayout = filteredList
                }
                Resource(state = ResourceState.SUCCESS, data = formFields)
            } else {
                Resource(state = ResourceState.SUCCESS, data = formFields)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getNearestHealthFacility(): Resource<ArrayList<Map<String, Any>>> {
        val healthFacilityList = roomHelper.getNearestHealthFacility()
        val dropDownList = ArrayList<Map<String, Any>>()
        for ((_, healthFacilityEntity) in healthFacilityList.withIndex()) {
            dropDownList.add(
                hashMapOf<String, Any>(
                    DefinedParams.NAME to healthFacilityEntity.name,
                    DefinedParams.id to healthFacilityEntity.fhirId.toString(),
                    DefinedParams.isDefault to healthFacilityEntity.isDefault,
                    DefinedParams.phoneNumber to (healthFacilityEntity.phoneNumber ?: ""),
                ),
            )
        }
        return Resource(state = ResourceState.SUCCESS, data = dropDownList)
    }

    suspend fun getHealthFacilityBasedOnVillageId(villageId: Long): Resource<List<HealthFacilityEntity>> =
        Resource(
            state = ResourceState.SUCCESS,
            data = roomHelper.getHealthFacilityBasedOnVillageId(villageId),
        )

    suspend fun getNearestHealthFacility(tag: String): Resource<LocalSpinnerResponse> =
        try {
            val response = roomHelper.getNearestHealthFacility()
            Resource(state = ResourceState.SUCCESS, LocalSpinnerResponse(tag, response))
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getUnSyncedAssessmentCount(): Int = roomHelper.getUnSyncedAssessmentCount()

    suspend fun updatePregnancyAncDetail(
        hhmLocalId: Long,
        visitCount: Long,
        clinicalDate: String?,
    ) {
        roomHelper.updatePregnancyAncDetail(hhmLocalId, visitCount, clinicalDate)
    }

    suspend fun getChildPatientId(parentId: Long): Long? = roomHelper.getChildPatientId(parentId)

    fun getAssessmentFormData(
        formType: String,
        workFlow: String,
    ): LiveData<String> = roomHelper.getAssessmentFormData(formType, workFlow)

    suspend fun getSymptomList(): List<SignsAndSymptomsEntity> = roomHelper.getSymptomList()

    suspend fun getMedicationParentComplianceList(): List<MedicalComplianceEntity> = roomHelper.getMedicalParentComplianceList()

    suspend fun getMedicationChildComplianceList(parentId: Long): List<MedicalComplianceEntity> = roomHelper.getMedicalChildComplianceList(parentId)

    suspend fun saveAssessmentInformation(createRequest: AssessmentNCDEntity) = roomHelper.saveAssessmentInformation(createRequest)

    suspend fun getAllAssessmentRecords(uploadStatus: Boolean) = roomHelper.getAllAssessmentRecords(uploadStatus)

    suspend fun deleteAssessmentList(isUploaded: Boolean) = roomHelper.deleteAssessmentList(isUploaded)

    suspend fun updateAssessmentUploadStatus(
        id: Long,
        uploadStatus: Boolean,
    ) = roomHelper.updateAssessmentUploadStatus(id, uploadStatus)

    suspend fun getAssessmentOfflineList(uploadStatus: Boolean) = roomHelper.getAllAssessmentRecords(uploadStatus)

    suspend fun createAssessmentNCD(request: JsonObject) = apiHelper.createAssessmentNCD(request)

    suspend fun getMentalQuestion(type: String): MentalHealthEntity = roomHelper.getModelQuestions(type)

    suspend fun getSymptomListByTypes(types: List<String>): List<SignsAndSymptomsEntity> =
        try {
            roomHelper.getSymptomListByTypes(types)
        } catch (_: Exception) {
            listOf()
        }
}
