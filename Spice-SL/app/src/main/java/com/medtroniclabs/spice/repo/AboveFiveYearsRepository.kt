package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.ExaminationsComplaintItems
import com.medtroniclabs.spice.data.model.AboveFiveYearsSubmitRequest
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AboveFiveYearsRepository @Inject constructor(
    private var roomHelper: RoomHelper,
    private var apiHelper: ApiHelper
) {
    suspend fun getStaticMetaData(
        aboveFiveYearsMetaLiveData: MutableLiveData<Resource<Boolean>>,
        menuType: String
    ){
        try {
            aboveFiveYearsMetaLiveData.postLoading()
            withContext(Dispatchers.IO){
                val response = apiHelper.getAboveFiveYearsMetaData()
                if (response.isSuccessful){
                    response.body()?.entity?.apply {
                        roomHelper.deleteExaminationsComplaints(menuType)
                        roomHelper.insertExaminationsComplaint(generateChipItemByType(presentingComplaints ,systemicExaminations, medicalSupplies, cost, patientStatus))
                        roomHelper.deleteDiagnosisList()
                        roomHelper.saveDiagnosisList(diseaseCategories)
                    }
                    SecuredPreference.putBoolean(
                        SecuredPreference.EnvironmentKey.IS_ABOVE_FIVE_YEARS_LOADED.name,
                        true
                    )
                    aboveFiveYearsMetaLiveData.postSuccess()
                }
            }
        } catch (e:Exception){
            e.printStackTrace()
            aboveFiveYearsMetaLiveData.postError()
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.IS_ABOVE_FIVE_YEARS_LOADED.name,
                false
            )
        }

    }

    private fun generateChipItemByType(
        presentingComplaints: List<ExaminationsComplaintItems>,
        systemicExaminations: List<ExaminationsComplaintItems>,
        medicalSupplies: List<ExaminationsComplaintItems>,
        cost: List<ExaminationsComplaintItems>,
        patientStatus: List<ExaminationsComplaintItems>
    ): List<ExaminationsComplaintItems> {
        val chipItemList = ArrayList<ExaminationsComplaintItems>()
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
        request: AboveFiveYearsSubmitRequest,
        aboveFiveYearsCreateResponse: MutableLiveData<Resource<AboveFiveYearsSummaryDetails>>
    ) {
        try {
            aboveFiveYearsCreateResponse.postLoading()
            val response = apiHelper.createAboveFiveYearsResult(request)
            if (response.isSuccessful) {
                val res = response.body()
                if (res?.status == true)
                    aboveFiveYearsCreateResponse.postSuccess(res.entity)
                else
                    aboveFiveYearsCreateResponse.postError()
            } else
                aboveFiveYearsCreateResponse.postError()
        } catch (e: Exception) {
            aboveFiveYearsCreateResponse.postError()
        }
    }

    suspend fun getSummaryDetailMetaItems(
        type: String,
        summaryMetaListItems: MutableLiveData<Resource<List<ExaminationsComplaintItems>>>
    ) {
        try {
            summaryMetaListItems.postLoading()
            val response = roomHelper.getSummaryDetailMetaItems(type)
            summaryMetaListItems.postSuccess(response)
        } catch (e: Exception) {
            summaryMetaListItems.postError()
        }
    }

    suspend fun getAboveFiveYearsSummaryDetails(
        request: AboveFiveYearsSummaryRequest,
        summaryDetailsLiveData: MutableLiveData<Resource<AboveFiveYearsSummaryDetails>>
    ) {
        try {
            summaryDetailsLiveData.postLoading()
            val response = apiHelper.getAboveFiveYearsSummaryDetails(request)
            if (response.isSuccessful) {
                response.body()?.entity?.let {
                    summaryDetailsLiveData.postSuccess(it)
                }
            } else {
                summaryDetailsLiveData.postError()
            }
        } catch (e: Exception) {
            summaryDetailsLiveData.postError()
        }
    }
}