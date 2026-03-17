package org.medtroniclabs.uhis.ncd.medicalreview.prescription.repo

import org.medtroniclabs.uhis.data.MedicationResponse
import org.medtroniclabs.uhis.data.MedicationSearchRequest
import org.medtroniclabs.uhis.data.Prescription
import org.medtroniclabs.uhis.data.PrescriptionListRequest
import org.medtroniclabs.uhis.data.RemovePrescriptionRequest
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.ncd.data.PredictionRequest
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import okhttp3.RequestBody
import javax.inject.Inject

class NCDPrescriptionRepo @Inject constructor(
    private var roomHelper: RoomHelper,
    private var apiHelper: ApiHelper,
) {
    suspend fun searchMedication(request: MedicationSearchRequest): Resource<ArrayList<MedicationResponse>> =
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

    suspend fun getDosageFrequencyList() = roomHelper.getDosageFrequencyList()

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

    suspend fun getUnitList(type: String) = roomHelper.getUnitList(type)

    suspend fun getDosageDurations() = roomHelper.getDosageDurations()

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

    suspend fun getPatientPrescriptionHistoryList(request: RemovePrescriptionRequest): Resource<ArrayList<Prescription>> =
        try {
            val response = apiHelper.getPatientPrescriptionHistoryList(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getNudgesList(prescriptionNudgeRequest: PredictionRequest) = apiHelper.getNudgesList(prescriptionNudgeRequest)
}
