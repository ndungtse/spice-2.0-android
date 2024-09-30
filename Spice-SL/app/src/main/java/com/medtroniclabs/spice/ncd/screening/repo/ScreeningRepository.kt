package com.medtroniclabs.spice.ncd.screening.repo

import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.MentalHealthEntity
import com.medtroniclabs.spice.db.entity.ScreeningEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import javax.inject.Inject

class ScreeningRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper
) {
    fun getUserHealthFacilityEntity(
    ): LiveData<List<HealthFacilityEntity>> {
        return roomHelper.getSites()
    }

    fun getFormData(
        formType: String,
    ): LiveData<String> {
        return roomHelper.getFormDataForNcd(formType)
    }

    fun getMentalQuestion(type:String) :  LiveData<MentalHealthEntity>{
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

    suspend fun createScreeningLog(createPatientRequest: JsonObject) =
        apiHelper.createScreening(createPatientRequest)

    suspend fun deleteUploadedScreeningRecords(todayDateTimeInMilliSeconds: Long) =
        roomHelper.deleteUploadedScreeningRecords(todayDateTimeInMilliSeconds)

    suspend fun updateScreeningRecordById(id: Long, uploadStatus: Boolean) =
        roomHelper.updateScreeningRecordById(id, uploadStatus)
}