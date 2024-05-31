package com.medtroniclabs.spice.ui.mypatients.repo

import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.ReferPatientAPIRequest
import com.medtroniclabs.spice.data.ReferPatientHealthFacilityItem
import com.medtroniclabs.spice.data.ReferPatientNameNumber
import com.medtroniclabs.spice.data.ReferPatientRequest
import com.medtroniclabs.spice.data.ReferPatientResult
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class ReferPatientRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper
) {

    suspend fun getHealthFacilityMetaData(
        districtId: String?
    ): Resource<List<ReferPatientHealthFacilityItem>> {
        try {
            val response = apiHelper.getHealthFacilityMetaData(ReferPatientAPIRequest(districtId))
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

    suspend fun getReferPatientMobileUserList(tenantId: String): Resource<List<ReferPatientNameNumber>> {
        try {
            val response = apiHelper.getReferPatientMobileUserList(ReferPatientRequest(tenantId))
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
        houseHoldId: String?,
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
        houseHoldId: String?,
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
                        householdId = houseHoldId,
                        villageId = villageId,
                        memberId = memberId,
                        referralTicketType = assessmentName.second
                    )
                }
        }
    }
    suspend fun getDefaultHealthFacilityDistrictId(): Resource<HealthFacilityEntity?>? {
        return try {
            val response = roomHelper.getDefaultHealthFacility()
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }
}