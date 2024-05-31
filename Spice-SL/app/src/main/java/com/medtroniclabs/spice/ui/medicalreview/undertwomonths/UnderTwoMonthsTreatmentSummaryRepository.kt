package com.medtroniclabs.spice.ui.medicalreview.undertwomonths

import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.UnderTwoMonthsSummaryDetails
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class UnderTwoMonthsTreatmentSummaryRepository @Inject constructor(
    private val apiHelper: ApiHelper
) {

    suspend fun getMedicalReviewForUnderTwoMonths(request: CreateUnderTwoMonthsResponse): Resource<UnderTwoMonthsSummaryDetails> {
        return try {
            val response = apiHelper.getMedicalReviewForUnderTwoMonths(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                val errorMessage = StringConverter.getErrorMessage(response.errorBody())
                Resource(state = ResourceState.ERROR, message = errorMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource(state = ResourceState.ERROR)
        }
    }


}