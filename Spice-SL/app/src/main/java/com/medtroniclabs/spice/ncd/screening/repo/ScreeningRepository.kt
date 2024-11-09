package com.medtroniclabs.spice.ncd.screening.repo

import androidx.lifecycle.LiveData
import com.medtroniclabs.spice.common.AppConstants
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.MentalHealthEntity
import com.medtroniclabs.spice.db.entity.ScreeningEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import okhttp3.RequestBody
import javax.inject.Inject

class ScreeningRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper
) {
    fun getUserHealthFacilityEntity(
    ): LiveData<List<HealthFacilityEntity>> {
        return roomHelper.getSites()
    }

    fun getMentalQuestion(type:String) :  LiveData<MentalHealthEntity?>{
        return  roomHelper.getMentalQuestion(type)
    }

    suspend fun savePatientScreeningInformation(screeningEntity: ScreeningEntity) =
        roomHelper.savePatientScreeningInformation(screeningEntity)

    fun getScreenedPatientCount(startDate: Long, endDate: Long, userId: String) =
        roomHelper.getScreenedPatientCount(startDate, endDate, userId)

    fun getScreenedPatientReferredCount(startDate: Long, endDate: Long, userId: String, isReferred: Boolean) =
        roomHelper.getScreenedPatientReferredCount(startDate, endDate, userId,isReferred)

    suspend fun getAllScreeningRecords(uploadStatus: Boolean): List<ScreeningEntity>? =
        roomHelper.getAllScreeningRecords(uploadStatus)

    suspend fun createScreeningLog(createPatientRequest: RequestBody) =
        apiHelper.createScreening(createPatientRequest)

    suspend fun deleteUploadedScreeningRecords(todayDateTimeInMilliSeconds: Long) =
        roomHelper.deleteUploadedScreeningRecords(todayDateTimeInMilliSeconds)

    suspend fun updateScreeningRecordById(id: Long, uploadStatus: Boolean) =
        roomHelper.updateScreeningRecordById(id, uploadStatus)

    suspend fun validatePatient(
        requestMap: HashMap<String, Any>,
        patientCreateReq: Pair<HashMap<String, Any>, List<FormLayout?>?>
    ): Resource<Pair<HashMap<String, Any>, List<FormLayout?>?>> {
        return try {

            val response = apiHelper.validatePatient(CommonUtils.validationRequest(requestMap))

            if (response.isSuccessful && response.body()?.status == true) {
                //Not a duplicate patient
                Resource(state = ResourceState.SUCCESS, data = patientCreateReq)
            } else if (response.code() == AppConstants.CONFLICT_ERROR_CODE) {
                //Duplicate patient found
                val duplicateEntity = StringConverter.getDuplicatePatientMap(response.errorBody())

                if (duplicateEntity.isNullOrEmpty())
                    Resource(state = ResourceState.ERROR, data = patientCreateReq)
                else
                    Resource(
                        state = ResourceState.ERROR,
                        data = Pair(duplicateEntity, null),
                        optionalData = true
                    )
            } else {
                Resource(
                    state = ResourceState.ERROR,
                    message = CommonUtils.getErrorMessage(response.errorBody()),
                    data = patientCreateReq
                )
            }
        } catch (_: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }
}