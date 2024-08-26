package com.medtroniclabs.spice.ncd.screening.repo

import androidx.lifecycle.LiveData
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.MentalHealthEntity
import com.medtroniclabs.spice.db.entity.ScreeningEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class ScreeningRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper
) {
    fun getUserHealthFacilityEntity(
    ): LiveData<List<HealthFacilityEntity>> {
        return roomHelper.getSites()
    }

    suspend fun getFormData(
        formType: String,
    ): Resource<String> {
        return try {
            val response = roomHelper.getFormData(formType)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    fun getMentalQuestion(type:String) :  LiveData<MentalHealthEntity>{
        return  roomHelper.getMentalQuestion(type)
    }

    suspend fun savePatientScreeningInformation(screeningEntity: ScreeningEntity) =
        roomHelper.savePatientScreeningInformation(screeningEntity)
}