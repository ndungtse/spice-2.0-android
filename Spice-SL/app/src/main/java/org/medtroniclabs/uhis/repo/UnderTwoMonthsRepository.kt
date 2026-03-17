package org.medtroniclabs.uhis.repo

import org.medtroniclabs.uhis.common.SecuredPreference
import org.medtroniclabs.uhis.common.StringConverter
import org.medtroniclabs.uhis.data.BirthHistoryRequest
import org.medtroniclabs.uhis.data.BirthHistoryResponse
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.model.medicalreview.CreateUnderTwoMonthsRequest
import org.medtroniclabs.uhis.model.medicalreview.CreateUnderTwoMonthsResponse
import org.medtroniclabs.uhis.model.medicalreview.SummaryDetails
import org.medtroniclabs.uhis.model.medicalreview.WazWhzScoreRequest
import org.medtroniclabs.uhis.model.medicalreview.WazWhzScoreResponse
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class UnderTwoMonthsRepository @Inject constructor(
    private val roomHelper: RoomHelper,
    private val apiHelper: ApiHelper,
) {
    suspend fun getStaticMetaData(): Resource<Boolean> =
        try {
            val response = apiHelper.getUnderTwoMonthsMetaData()
            if (response.isSuccessful) {
                response.body()?.entity?.apply {
                    roomHelper.deleteDiagnosisList(MedicalReviewTypeEnums.UNDER_TWO_MONTHS.name)
                    roomHelper.saveDiagnosisList(diseaseCategories)
                    roomHelper.deleteExaminationsList(MedicalReviewTypeEnums.UNDER_TWO_MONTHS.name)
                    roomHelper.saveExaminationsList(examinations)
                    roomHelper.deleteExaminationsComplaints(MedicalReviewTypeEnums.UNDER_TWO_MONTHS.name)
                    roomHelper.insertExaminationsComplaint(
                        generateChipItemByType(
                            patientStatus,
                            immunisationStatus,
                        ),
                    )
                }
                SecuredPreference.putBoolean(
                    SecuredPreference.EnvironmentKey.IS_UNDER_TWO_MONTHS_LOADED.name,
                    true,
                )
                Resource(state = ResourceState.SUCCESS, true)
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_UNDER_TWO_MONTHS_LOADED.name,
                false,
            )
            Resource(state = ResourceState.ERROR)
        }

    suspend fun createMedicalReviewForUnderTwoMonths(request: CreateUnderTwoMonthsRequest): Resource<CreateUnderTwoMonthsResponse> =
        try {
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

    suspend fun getBirthHistoryDetailsUnderTwoMonths(request: BirthHistoryRequest): Resource<BirthHistoryResponse> =
        try {
            val response = apiHelper.getBirthHistoryDetails(request)
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

    suspend fun getSummaryDetailMetaItems(type: String): Resource<List<MedicalReviewMetaItems>> =
        try {
            val response = roomHelper.getSummaryDetailMetaItems(type)
            val filteredAndSortedResponse = response
                .filter { item -> item.category == MedicalReviewTypeEnums.patient_status.name }
                .sortedBy { it.displayOrder }
            Resource(state = ResourceState.SUCCESS, data = filteredAndSortedResponse)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getImmunisationStatusMetaItems(type: String): Resource<List<MedicalReviewMetaItems>> =
        try {
            val response = roomHelper.getSummaryDetailMetaItems(type)
            val filteredAndSortedResponse = response
                .filter { item -> item.category == MedicalReviewTypeEnums.immunisation_status.name }
                .sortedBy { it.displayOrder }
            Resource(state = ResourceState.SUCCESS, data = filteredAndSortedResponse)
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    private fun generateChipItemByType(
        patientStatus: List<MedicalReviewMetaItems>,
        immunisationStatus: ArrayList<MedicalReviewMetaItems>,
    ): List<MedicalReviewMetaItems> {
        val chipItemList = ArrayList<MedicalReviewMetaItems>()
        patientStatus.forEach { it.type = MedicalReviewTypeEnums.UNDER_TWO_MONTHS.name }
        chipItemList.addAll(patientStatus)

        immunisationStatus.forEach { it.type = MedicalReviewTypeEnums.UNDER_TWO_MONTHS.name }
        chipItemList.addAll(immunisationStatus)
        return chipItemList
    }

    suspend fun getMedicalReviewForUnderTwoMonths(request: CreateUnderTwoMonthsResponse): Resource<SummaryDetails> =
        try {
            val response =
                apiHelper.getMedicalReviewForUnderTwoMonths(request)
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

    suspend fun getWazWhzScore(request: WazWhzScoreRequest): Resource<WazWhzScoreResponse> =
        try {
            val response = apiHelper.getWazWhzScore(request)
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
