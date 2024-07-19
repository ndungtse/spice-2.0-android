package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.data.MedicalReviewSummarySubmitRequest
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class MedicalReviewSummaryRepository @Inject constructor(
    private var apiHelper: ApiHelper
) {

    suspend fun createSummarySubmit(
        patientId: String,
        patientReference: String,
        memberId: String,
        id: String,
        cost: String? = null,
        medicalSupplies: List<String>? = null,
        patientStatus: String,
        nextVisitDate: String,
        referralTicketType: String,
        assessmentName: String,
        householdId: String?,
        villageId: String
    ): Resource<HashMap<String, Any>> {
        return try {
            val request = MedicalReviewSummarySubmitRequest(
                patientId = patientId,
                memberId = memberId,
                id = id,
                patientStatus = patientStatus,
                nextVisitDate = nextVisitDate,
                referralTicketType = referralTicketType,
                assessmentName = assessmentName,
                householdId = householdId,
                villageId = villageId,
                provenance = ProvanceDto(),
                cost = cost,
                medicalSupplies = medicalSupplies,
                patientReference = patientReference
            )
            val response = request.let { apiHelper.createSummarySubmit(it) }
            if (response.isSuccessful) {
                val res = response.body()
                if (res?.status == true) {
                    Resource(state = ResourceState.SUCCESS)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            } else {
                Resource(state = ResourceState.ERROR)
            }

        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }
}