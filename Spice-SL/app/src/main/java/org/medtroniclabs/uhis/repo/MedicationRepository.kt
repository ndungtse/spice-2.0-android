package org.medtroniclabs.uhis.repo

import okhttp3.RequestBody
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.MedicationGroupSearchRequest
import org.medtroniclabs.uhis.data.MedicationResponse
import org.medtroniclabs.uhis.data.MedicationSearchRequest
import org.medtroniclabs.uhis.data.Prescription
import org.medtroniclabs.uhis.data.PrescriptionListRequest
import org.medtroniclabs.uhis.data.RemovePrescriptionRequest
import org.medtroniclabs.uhis.db.entity.FrequencyEntity
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import javax.inject.Inject

class MedicationRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper,
) {
    suspend fun searchMedicationByName(request: MedicationSearchRequest): Resource<ArrayList<MedicationResponse>> =
        try {
            val response = apiHelper.searchMedicationByName(request)
            if (response.isSuccessful) {
                Resource(ResourceState.SUCCESS, response.body()?.entityList)
            } else {
                Resource(ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }

    suspend fun createPrescriptionRequest(body: RequestBody): Resource<Map<String, Any>> =
        try {
            val response = apiHelper.createPrescriptionRequest(body)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getPrescriptionList(request: PrescriptionListRequest): Resource<ArrayList<Prescription>> =
        try {
            val response = apiHelper.getPrescriptionList(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun removePrescription(request: RemovePrescriptionRequest): Resource<Map<String, Any>> =
        try {
            val response = apiHelper.removePrescription(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun removeCommunityPrescription(request: List<RemovePrescriptionRequest>): Resource<Map<String, Any>> =
        try {
            val response = apiHelper.removeCommunityPrescription(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getFrequencyList(): Resource<List<FrequencyEntity>> =
        try {
            val response = roomHelper.getFrequencyList()
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getInstructionList(): Resource<List<MedicalReviewMetaItems>> =
        try {
            val response = roomHelper.getInstructionList()
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun searchMedicationGroupByName(request: MedicationGroupSearchRequest): Resource<ArrayList<MedicationResponse>> =
        try {
            val response = apiHelper.searchMedicationGroupByName(request)
            if (response.isSuccessful) {
                Resource(ResourceState.SUCCESS, response.body()?.entityList)
            } else {
                Resource(ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }
}
