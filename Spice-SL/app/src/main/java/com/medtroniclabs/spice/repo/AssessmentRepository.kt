package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.db.entity.AssessmentEntity
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.formgeneration.model.FormResponse
import com.medtroniclabs.spice.network.resource.Resource
import javax.inject.Inject

class AssessmentRepository @Inject constructor(
    private var roomHelper: RoomHelper
) {

    private val resultHashMap = HashMap<String, Any>()

    suspend fun saveAssessment(
        resultData: String,
        householdId: Long,
        assessmentSaveLiveData: MutableLiveData<Resource<String>>,
        menuId: String?,
        memberId: Long
    ) {
        try {
            val assessmentEntity = menuId?.let { menu ->
                AssessmentEntity(
                    memberId = memberId,
                    householdId = householdId,
                    assessmentType = menu.lowercase(),
                    assessmentDetails = resultData,
                    userId = 1
                )
            }
            assessmentEntity?.let {
                roomHelper.saveAssessment(assessmentEntity)
            }
            assessmentSaveLiveData.postSuccess(resultData)
        } catch (e: Exception) {
            assessmentSaveLiveData.postError()
        }
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

    suspend fun insertSymptoms() {
        try {
            val signs = listOf(
                "Sunken Eyes",
                "No Tears when crying",
                "Little or no urine",
                "Unusually sleepy or uncoscious",
                "Unable to drinking poorly",
                "Skin pinch going back very slowly",
                "No Symptoms",
                "Other"
            )
            val signsList = ArrayList<SignsAndSymptomsEntity>()
            for (i in signs.indices) {
                signsList.add(
                    SignsAndSymptomsEntity(
                        _id = (i + 1).toLong(),
                        symptom = signs[i],
                        type = DefinedParams.DiarrhoeaSigns,
                        displayOrder = i + 1
                    )
                )
            }
            roomHelper.insertSymptomList(signsList)
        } catch (_: Exception) {
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
}