package com.medtroniclabs.spice.ui.mypatients.repo

import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.ReferPatientAPIRequest
import com.medtroniclabs.spice.data.ReferPatientHealthFacilityItem
import com.medtroniclabs.spice.data.ReferPatientNameNumber
import com.medtroniclabs.spice.data.ReferPatientResult
import com.medtroniclabs.spice.data.ReferPatientRequest
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class ReferPatientRepository @Inject constructor(
    private val apiHelper: ApiHelper
) {

    suspend fun getHealthFacilityMetaData(
        request: ReferPatientAPIRequest
    ): Resource<List<ReferPatientHealthFacilityItem>> {
        try {
            val response = apiHelper.getHealthFacilityMetaData(request)
            if (response.isSuccessful) {
                response.body()?.entityList?.let {
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

    suspend fun getReferPatientMobileUserList(tenantId: ReferPatientRequest): Resource<List<ReferPatientNameNumber>> {
        try {
            val response = apiHelper.getReferPatientMobileUserList(tenantId)
            if (response.isSuccessful) {
                response.body()?.entityList?.let {
                    return Resource(state = ResourceState.SUCCESS, it)
                }
            }
        } catch (e: Exception) {
            return Resource(state = ResourceState.ERROR)
        }
        return Resource(state = ResourceState.ERROR)
    }
    suspend fun createReferPatientResult(
        details: AboveFiveYearsSummaryDetails,
        selectedItems: Triple<String?, String?, String?>,
        assessmentName: Pair<String?, String>,
        patientId: String?,
        houseHoldId: Long?,
        villageId: String?,
        memberId: String?
    ): Resource<HashMap<String,Any>> {
        try {
            val request = createReferPatientRequest(
                details,
                selectedItems,
                assessmentName,
                patientId,
                houseHoldId,
                villageId,
                memberId
            )
            val response = request?.let { apiHelper.createReferPatientResult(it) }
            if (response != null && response.isSuccessful) {
                response.body()?.entity?.let {
                    return Resource(state = ResourceState.SUCCESS, it)
                }
            }
        } catch (e: Exception) {
            return Resource(state = ResourceState.ERROR)
        }
        return Resource(state = ResourceState.ERROR)
    }
    private fun createReferPatientRequest(
        details: AboveFiveYearsSummaryDetails,
        selectedItems: Triple<String?, String?, String?>,
        assessmentName: Pair<String?, String>,
        patientId: String?,
        houseHoldId: Long?,
        villageId: String?,
        memberId: String?
    ): ReferPatientResult? {
        return details.patientReference?.let { patientReference ->
            details.encounterId?.let { encounterId ->
                    ReferPatientResult(
                        encounterId = encounterId,
                        type = MedicalReviewTypeEnums.medicalReview.name,
                        referredReason = selectedItems.third,
                        referredSiteId = selectedItems.first,
                        referredClinicianId = selectedItems.second,
                        patientReference = patientReference,
                        referred = true,
                        provenance = ProvanceDto(
                            createdDateTime = DateUtils.getCurrentDateAndTime(
                                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                            )
                        ),
                        patientStatus = DefinedParams.REFERRED,
                        currentPatientStatus = DefinedParams.REFERRED,
                        assessmentName = assessmentName.first,
                        patientId = patientId,
                        householdId = houseHoldId.toString(), //Todo : this should be long
                        villageId = villageId,
                        memberId = memberId,
                        referralTicketType = assessmentName.second
                    )
                }
        }
    }
}