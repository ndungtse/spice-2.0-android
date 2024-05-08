package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.data.MedicationSearchRequest
import com.medtroniclabs.spice.network.ApiHelper
import okhttp3.RequestBody
import javax.inject.Inject

class MedicationRepository @Inject constructor(
    private var apiHelper: ApiHelper
) {
    suspend fun searchMedicationByName(request: MedicationSearchRequest) =
        apiHelper.searchMedicationByName(request)

    suspend fun createPrescriptionRequest(body: RequestBody) =
        apiHelper.createPrescriptionRequest(body)

}