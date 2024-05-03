package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.AboveFiveYearsSummarySubmitRequest
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.AboveFiveYearsSubmitRequest
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class AboveFiveYearsRepository @Inject constructor(
    private var roomHelper: RoomHelper,
    private var apiHelper: ApiHelper
) {
    suspend fun getStaticMetaData(
        menuType: String
    ): Resource<Boolean> {
        return try {
            val response = apiHelper.getAboveFiveYearsMetaData()
            if (response.isSuccessful) {
                response.body()?.entity?.apply {
                    roomHelper.deleteExaminationsComplaints(menuType)
                    roomHelper.insertExaminationsComplaint(
                        generateChipItemByType(
                            presentingComplaints,
                            systemicExaminations,
                            medicalSupplies,
                            cost,
                            patientStatus
                        )
                    )
                    roomHelper.deleteDiagnosisList()
                    roomHelper.saveDiagnosisList(diseaseCategories)
                }
                SecuredPreference.putBoolean(
                    SecuredPreference.EnvironmentKey.IS_ABOVE_FIVE_YEARS_LOADED.name,
                    true
                )
                Resource(state = ResourceState.SUCCESS, true)
            } else {
                Resource(state = ResourceState.ERROR)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_ABOVE_FIVE_YEARS_LOADED.name,
                false
            )
            Resource(state = ResourceState.ERROR)
        }

    }

    private fun generateChipItemByType(
        presentingComplaints: List<MedicalReviewMetaItems>,
        systemicExaminations: List<MedicalReviewMetaItems>,
        medicalSupplies: List<MedicalReviewMetaItems>,
        cost: List<MedicalReviewMetaItems>,
        patientStatus: List<MedicalReviewMetaItems>
    ): List<MedicalReviewMetaItems> {
        val chipItemList = ArrayList<MedicalReviewMetaItems>()
        presentingComplaints.forEach { it.category = MedicalReviewTypeEnums.PresentingComplaints.name }
        systemicExaminations.forEach { it.category = MedicalReviewTypeEnums.SystemicExaminations.name }
        patientStatus.forEach { it.type = MedicalReviewTypeEnums.AboveFiveYears.name }
        chipItemList.addAll(presentingComplaints)
        chipItemList.addAll(systemicExaminations)
        chipItemList.addAll(medicalSupplies)
        chipItemList.addAll(cost)
        chipItemList.addAll(patientStatus)
        return chipItemList
    }

    suspend fun createAboveFiveYears(
        request: AboveFiveYearsSubmitRequest
    ): Resource<AboveFiveYearsSummaryDetails> {
        return try {
            val response = apiHelper.createAboveFiveYearsResult(request)
            if (response.isSuccessful) {
                val res = response.body()
                if (res?.status == true) {
                    Resource(state = ResourceState.SUCCESS, data = res.entity)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            } else{
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getSummaryDetailMetaItems(
        type: String
    ): Resource<List<MedicalReviewMetaItems>> {
        return try {
            val response = roomHelper.getSummaryDetailMetaItems(type)
            Resource(state = ResourceState.SUCCESS, data = response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getAboveFiveYearsSummaryDetails(
        request: AboveFiveYearsSummaryRequest
    ): Resource<AboveFiveYearsSummaryDetails> {
        return try {
            val response = apiHelper.getAboveFiveYearsSummaryDetails(request)
            if (response.isSuccessful) {
                Resource(state = ResourceState.SUCCESS, data = response.body()?.entity)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun aboveFiveYearsSummaryCreate(
        request: AboveFiveYearsSummarySubmitRequest,
    ): Resource<HashMap<String, Any>> {
        return try {
            val response = apiHelper.aboveFiveYearsSummaryCreate(request)
            if (response.isSuccessful) {
                val res = response.body()
                if (res?.status == true) {
                    Resource(state = ResourceState.SUCCESS)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            } else{
                Resource(state = ResourceState.ERROR)
            }

        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }

    suspend fun getDiagnosisList() : Resource<List<DiseaseCategoryItems>> {
      return  try {
            val response = roomHelper.getDiagnosisList()
            Resource(state = ResourceState.SUCCESS, response)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
    }
}