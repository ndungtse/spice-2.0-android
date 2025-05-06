package com.medtroniclabs.spice.ui.medicalreview.hiv.repo

import android.annotation.SuppressLint
import com.medtroniclabs.spice.common.ConsentFormType
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.HivScreeningRequest
import com.medtroniclabs.spice.data.model.HivScreeningResponse
import com.medtroniclabs.spice.data.model.HivScreeningSummaryResponse
import com.medtroniclabs.spice.db.entity.ConsentForm
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class HivMedicalReviewRepo @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {
    suspend fun getStaticMetaData():Resource<Boolean>{
        return try {
            val response = apiHelper.getHivStaticData()
            if (response.isSuccessful) {
                response.body()?.entity?.apply {
                    roomHelper.insertExaminationsComplaint(
                        generateChipItemByType(
                            hivHistory,
                            populationType,
                            hivTestDurations,
                            entryPoint
                        )
                    )
                }
                SecuredPreference.putBoolean(
                    SecuredPreference.EnvironmentKey.IS_HIV_DATA_LOADED.name,
                    true
                )
                Resource(state = ResourceState.SUCCESS, true)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        }catch (e: Exception) {
            e.printStackTrace()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_HIV_DATA_LOADED.name,
                false
            )
            Resource(state = ResourceState.ERROR)
        }
    }
    private fun generateChipItemByType(hivHistory: List<MedicalReviewMetaItems>,
                                       populationType:List<MedicalReviewMetaItems>,
                                       hivTestDuration: List<MedicalReviewMetaItems>,
                                       entryPoint: List<MedicalReviewMetaItems>
    ): List<MedicalReviewMetaItems> {
        val chipItemList = ArrayList<MedicalReviewMetaItems>()
        chipItemList.addAll(hivHistory)
        chipItemList.addAll(populationType)
        chipItemList.addAll(hivTestDuration)
        chipItemList.addAll(entryPoint)
        return chipItemList
    }
    suspend fun getHivMetaItems(): Resource<List<MedicalReviewMetaItems>> {
        return try {
            val response = roomHelper.getHivMetaData()
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getConsentForm() : ConsentForm? {
        return roomHelper.getConsentFormByType(ConsentFormType.HIV)
    }

    suspend fun createHivScreening(request: HivScreeningRequest): Resource<HivScreeningResponse> {
        return try {
            val response = apiHelper.createHivScreening(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                val errorMessage = StringConverter.getErrorMessage(response.errorBody())
                Resource(state = ResourceState.ERROR, message = errorMessage)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getHivScreeningDetails(request: HivScreeningResponse): Resource<HivScreeningSummaryResponse> {
        return try {
            val response = apiHelper.getHivScreeningDetails(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                val errorMessage = StringConverter.getErrorMessage(response.errorBody())
                Resource(state = ResourceState.ERROR, message = errorMessage)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

}