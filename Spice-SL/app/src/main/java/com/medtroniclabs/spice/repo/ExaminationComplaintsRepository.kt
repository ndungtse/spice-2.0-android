package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class ExaminationComplaintsRepository @Inject constructor(
    private var roomHelper: RoomHelper,
) {
    suspend fun getComplaintsListByType(type: String): Resource<List<MedicalReviewMetaItems>> =
        try {
            val response = roomHelper.getExaminationsComplaintByType(type)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
}
