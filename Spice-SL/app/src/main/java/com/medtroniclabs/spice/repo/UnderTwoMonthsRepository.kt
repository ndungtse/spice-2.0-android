package com.medtroniclabs.spice.repo

import CreateUnderTwoMonthsRequest
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class UnderTwoMonthsRepository @Inject constructor(
    private val roomHelper: RoomHelper,
    private val apiHelper: ApiHelper
) {
    suspend fun getStaticMetaData(): Resource<Boolean> {
        return try {
                val response = apiHelper.getUnderTwoMonthsMetaData()
                if (response.isSuccessful) {
                    response.body()?.entity?.apply {
                        roomHelper.deleteDiagnosisList()
                        roomHelper.saveDiagnosisList(diseaseCategories)
                        roomHelper.deleteExaminationsList()
                        roomHelper.saveExaminationsList(examinations)
                    }
                    SecuredPreference.putBoolean(
                        SecuredPreference.EnvironmentKey.IS_UNDER_TWO_MONTHS_LOADED.name,
                        true
                    )
                    Resource(state = ResourceState.SUCCESS, true)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_UNDER_TWO_MONTHS_LOADED.name,
                false
            )
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun createMedicalReviewForUnderTwoMonths(request: CreateUnderTwoMonthsRequest): Resource<CreateUnderTwoMonthsResponse> {
        return try {
            val response = apiHelper.createMedicalReviewForUnderTwoMonths(request)
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