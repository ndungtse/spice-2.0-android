package org.medtroniclabs.uhis.repo

import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
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
