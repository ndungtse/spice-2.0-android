package org.medtroniclabs.uhis.ui.mypatients.repo

import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.data.ReferPatientAPIRequest
import org.medtroniclabs.uhis.data.ReferPatientHealthFacilityItem
import org.medtroniclabs.uhis.data.ReferPatientNameNumber
import org.medtroniclabs.uhis.data.ReferPatientRequest
import org.medtroniclabs.uhis.data.ReferPatientResult
import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto
import org.medtroniclabs.uhis.db.entity.HealthFacilityEntity
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class ReferPatientRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper,
) {
    suspend fun getHealthFacilityMetaData(districtId: String?): Resource<List<ReferPatientHealthFacilityItem>> {
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
        patientReference: String?,
        encounterId: String?,
        selectedItems: Triple<String?, String?, String?>,
        assessmentName: Pair<String?, String>,
        patientId: String?,
        houseHoldId: String?,
        villageId: String?,
        memberId: String?,
        tbIMRCompleted: Boolean? = null,
    ): Resource<HashMap<String, Any>> {
        try {
            val request = createReferPatientRequest(
                patientReference,
                encounterId,
                selectedItems,
                assessmentName,
                patientId,
                houseHoldId,
                villageId,
                memberId,
                tbIMRCompleted = tbIMRCompleted,
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
        patientReference: String?,
        encounterId: String?,
        selectedItems: Triple<String?, String?, String?>,
        assessmentName: Pair<String?, String>,
        patientId: String?,
        houseHoldId: String?,
        villageId: String?,
        memberId: String?,
        tbIMRCompleted: Boolean? = null,
    ): ReferPatientResult =
        ReferPatientResult(
            encounterId = encounterId,
            type = MedicalReviewTypeEnums.medicalReview.name,
            referredReason = selectedItems.third,
            referredSiteId = selectedItems.first,
            referredClinicianId = selectedItems.second,
            patientReference = patientReference,
            referred = true,
            provenance = ProvanceDto(),
            patientStatus = DefinedParams.REFERRED,
            currentPatientStatus = DefinedParams.REFERRED,
            assessmentName = assessmentName.first,
            patientId = patientId,
            householdId = houseHoldId,
            villageId = villageId,
            memberId = memberId,
            category = assessmentName.second,
            tbIMRCompleted = tbIMRCompleted,
        )

    suspend fun getDefaultHealthFacilityDistrictId(): Resource<HealthFacilityEntity?>? =
        try {
            val response = roomHelper.getDefaultHealthFacility()
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
}
