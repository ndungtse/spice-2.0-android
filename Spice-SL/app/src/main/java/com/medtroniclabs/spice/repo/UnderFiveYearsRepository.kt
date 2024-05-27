package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class UnderFiveYearsRepository @Inject constructor(
    private var roomHelper: RoomHelper,
    private var apiHelper: ApiHelper
) {

    suspend fun getStaticMetaData(
        menuType: String
    ): Resource<Boolean> {
        return try {
            val response = apiHelper.getUnderFiveYearsMetaData()
            if (response.isSuccessful) {
                response.body()?.entity?.apply {
                    roomHelper.deleteExaminationsComplaints(menuType)
                    roomHelper.insertExaminationsComplaint(
                        generateChipItemByType(systemicExaminations)
                    )
                    roomHelper.deleteExaminationsList(MedicalReviewTypeEnums.UnderFiveYears.name)
                    roomHelper.saveExaminationsList(examinations)
                }
                SecuredPreference.putBoolean(
                    SecuredPreference.EnvironmentKey.IS_UNDER_FIVE_YEARS_LOADED.name,
                    true
                )
                Resource(state = ResourceState.SUCCESS, true)
            } else {
                Resource(state = ResourceState.ERROR)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_UNDER_FIVE_YEARS_LOADED.name,
                false
            )
            Resource(state = ResourceState.ERROR)
        }

    }

    private fun generateChipItemByType(systemicExaminations: List<MedicalReviewMetaItems>): List<MedicalReviewMetaItems> {
        val chipItemList = ArrayList<MedicalReviewMetaItems>()
        systemicExaminations.forEach {
            it.category = MedicalReviewTypeEnums.SystemicExaminations.name
        }
        chipItemList.addAll(systemicExaminations)
        return chipItemList
    }

}