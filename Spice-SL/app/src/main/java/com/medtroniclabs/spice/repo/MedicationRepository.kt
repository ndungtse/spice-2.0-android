package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.data.MedicationSearchRequest
import com.medtroniclabs.spice.network.ApiHelper
import javax.inject.Inject

class MedicationRepository @Inject constructor(
    private var apiHelper: ApiHelper
) {
    suspend fun searchMedicationByName(request: MedicationSearchRequest) =
        apiHelper.searchMedicationByName(request)
}