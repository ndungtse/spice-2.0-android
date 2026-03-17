package org.medtroniclabs.uhis.ui.medicalreview.underfiveyears

import androidx.lifecycle.LiveData
import org.medtroniclabs.uhis.common.StringConverter
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.model.medicalreview.CreateUnderTwoMonthsResponse
import org.medtroniclabs.uhis.model.medicalreview.SummaryDetails
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import javax.inject.Inject

class UnderFiveYearsTreatmentSummaryRepository @Inject constructor(
    private val apiHelper: ApiHelper,
    private val roomHelper: RoomHelper,
) {
    suspend fun getUnderFiveYearsSummaryDetails(request: CreateUnderTwoMonthsResponse): Resource<SummaryDetails> =
        try {
            val response = apiHelper.getUnderFiveYearsSummaryDetails(request)
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

    fun getExaminationsComplaints(
        category: String,
        type: String,
    ): LiveData<List<MedicalReviewMetaItems>> = roomHelper.getExaminationsComplaintsForAnc(category, type)
}
