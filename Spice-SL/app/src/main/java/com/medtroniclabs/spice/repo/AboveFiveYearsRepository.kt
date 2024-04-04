package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.SecuredPreference
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
                        roomHelper.insertExaminationsComplaint(generateChipItemByType(presentingComplaints ,systemicExaminations, medicalSupplies))
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
        medicalSupplies: List<ExaminationsComplaintItems>
    ): List<ExaminationsComplaintItems> {
        val chipItemList = ArrayList<ExaminationsComplaintItems>()
        presentingComplaints.forEach { it.category = MedicalReviewTypeEnums.PresentingComplaints.name }
        systemicExaminations.forEach { it.category = MedicalReviewTypeEnums.SystemicExaminations.name }
        chipItemList.addAll(presentingComplaints)
        chipItemList.addAll(systemicExaminations)
        chipItemList.addAll(medicalSupplies)
        return chipItemList
    }

    suspend fun createAboveFiveYears(
        request: AboveFiveYearsSubmitRequest,
        aboveFiveYearsCreateResponse: MutableLiveData<Resource<HashMap<String, Any>>>
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
}