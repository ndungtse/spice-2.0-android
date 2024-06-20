package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.common.CommonUtils.getTicketType
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.data.PatientStatusRequest
import com.medtroniclabs.spice.data.PatientStatusResponse
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class PatientStatusRepository @Inject constructor(
    private var apiHelper: ApiHelper
) {
    suspend fun getPatientStatusDetails(patientDetails: PatientListRespModel, menuType: String): Resource<PatientStatusResponse> {
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

    private fun createPatientStatusRequest(patientDetails: PatientListRespModel, menuType: String): PatientStatusRequest? {
        return patientDetails.memberId?.let { patientMemberId ->
            getTicketType(menuType)?.let {workflowType ->
                PatientStatusRequest(
                    memberId = patientMemberId,
                    type = MedicalReviewTypeEnums.medicalReview.name,
                    gender = patientDetails.gender,
                    ticketType = workflowType,
                    isPregnant = patientDetails.isPregnant ?: false,
                    provenance = ProvanceDto(
                        createdDateTime = DateUtils.getCurrentDateAndTime(
                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                        )
                    )
                )
            }
        }
    }
}