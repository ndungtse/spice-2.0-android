package org.medtroniclabs.uhis.repo

import org.medtroniclabs.uhis.common.CommonUtils.getTicketType
import org.medtroniclabs.uhis.data.PatientStatusRequest
import org.medtroniclabs.uhis.data.PatientStatusResponse
import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto
import org.medtroniclabs.uhis.model.PatientListRespModel
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class PatientStatusRepository @Inject constructor(
    private var apiHelper: ApiHelper,
) {
    suspend fun getPatientStatusDetails(
        patientDetails: PatientListRespModel,
        menuType: String,
    ): Resource<PatientStatusResponse> {
        try {
            val request = createPatientStatusRequest(patientDetails, menuType)
            val response = request?.let { apiHelper.getPatientStatus(it) }
            if (response?.isSuccessful == true) {
                response.body()?.entity?.let {
                    return Resource(state = ResourceState.SUCCESS, it)
                }
            } else {
                return Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            return Resource(state = ResourceState.ERROR)
        }
        return Resource(state = ResourceState.ERROR)
    }

    private fun createPatientStatusRequest(
        patientDetails: PatientListRespModel,
        menuType: String,
    ): PatientStatusRequest? =
        patientDetails.memberId?.let { patientMemberId ->
            getTicketType(menuType)?.let { workflowType ->
                PatientStatusRequest(
                    patientId = patientDetails.patientId,
                    memberId = patientMemberId,
                    type = MedicalReviewTypeEnums.medicalReview.name,
                    gender = patientDetails.gender,
                    ticketType = workflowType,
                    isPregnant = patientDetails.isPregnant ?: false,
                    encounterType = menuType,
                    provenance = ProvanceDto(),
                )
            }
        }
}
