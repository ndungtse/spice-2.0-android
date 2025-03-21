package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.db.entity.TreatmentDetailsEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class TreatmentDetailsRepository @Inject constructor(
    private var roomHelper: RoomHelper
) {

    suspend fun insertOrUpdateTreatmentDetails(
        treatmentDetails: TreatmentDetailsEntity
    ): Resource<Long> {
        return try {
            val existTreatment = roomHelper.getTreatmentDetails(memberId = treatmentDetails.memberId.toLong())
            if(existTreatment != null){
                val updateTreatment = treatmentDetails.copy(id = existTreatment.id)
                val response = roomHelper.updateTreatmentDetails(updateTreatment)
                if(response > 0){
                    Resource(state = ResourceState.SUCCESS, data = response.toLong())
                }else{
                    Resource(state = ResourceState.ERROR, data = null)
                }
            }else{
                val response = roomHelper.insertTreatmentDetails(treatmentDetails)
                Resource(state = ResourceState.SUCCESS, data = response)
            }

        }catch (e:Exception){
            Resource(state = ResourceState.ERROR, data = null)
        }
    }

    suspend fun getTreatmentDetails(hhmId:Long):Resource<TreatmentDetailsEntity>{
        return try {
            val response = roomHelper.getTreatmentDetails(memberId = hhmId)
            Resource(state = ResourceState.SUCCESS, data = response)
        }catch (e:Exception){
            Resource(state = ResourceState.ERROR, data = null)
        }
    }
}