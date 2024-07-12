package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.data.MedicationResponse
import com.medtroniclabs.spice.data.MedicationSearchRequest
import com.medtroniclabs.spice.data.Prescription
import com.medtroniclabs.spice.data.PrescriptionListRequest
import com.medtroniclabs.spice.data.RemovePrescriptionRequest
import com.medtroniclabs.spice.db.entity.FrequencyEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import okhttp3.RequestBody
import javax.inject.Inject

class MedicationRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {
    suspend fun searchMedicationByName(request: MedicationSearchRequest): Resource<ArrayList<MedicationResponse>> {
        return try {
            val response = apiHelper.searchMedicationByName(request)
            if (response.isSuccessful) {
                Resource(ResourceState.SUCCESS, response.body()?.entityList)
            } else {
                Resource(ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(ResourceState.ERROR)
        }
    }

    suspend fun createPrescriptionRequest(body: RequestBody): Resource<Map<String, Any>> {
        return try {
            val response = apiHelper.createPrescriptionRequest(body)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }


    suspend fun getPrescriptionList(request: PrescriptionListRequest): Resource<ArrayList<Prescription>> {
        return try {
            val response = apiHelper.getPrescriptionList(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun removePrescription(request: RemovePrescriptionRequest): Resource<Map<String, Any>> {
        return try {
            val response = apiHelper.removePrescription(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getFrequencyList(): Resource<List<FrequencyEntity>> {
        return try {
            val response = roomHelper.getFrequencyList()
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

}