package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.EpiGroupName
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.medicalreview.RequestCreateImmunisation
import com.medtroniclabs.spice.model.medicalreview.RequestImmunisationSummaryCreate
import com.medtroniclabs.spice.model.medicalreview.RequestImmunisationSummaryDetail
import com.medtroniclabs.spice.model.medicalreview.RequestVaccinationList
import com.medtroniclabs.spice.model.medicalreview.ResponseCreateImmunisation
import com.medtroniclabs.spice.model.medicalreview.ResponseImmunisationSummaryCreate
import com.medtroniclabs.spice.model.medicalreview.ResponseImmunisationSummaryDetails
import com.medtroniclabs.spice.model.medicalreview.VaccinationDetail
import com.medtroniclabs.spice.model.medicalreview.VaccinationGroupItem
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import javax.inject.Inject

class ImmunisationRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {

    suspend fun getImmunisationDetails(
        request: RequestVaccinationList,
        response: MutableLiveData<Resource<List<VaccinationGroupItem>>>
    ) {
        response.postLoading()
        try {
            val apiResponse = apiHelper.getVaccinationList(request)
            if (apiResponse.isSuccessful) {
                response.postSuccess(getVaccinationGroupItems(apiResponse.body()?.entityList))
            } else {
                response.postError()
            }
        } catch (e: Exception) {
            response.postError(e.message)
        }
    }


    private fun getVaccinationGroupItems(vaccinationList: ArrayList<VaccinationDetail>?): List<VaccinationGroupItem> {
        // Need to do Schedule Date based on Date of Birth
        val list = mutableListOf<VaccinationGroupItem>()

        val groupByType = vaccinationList?.groupBy { it.type }
        groupByType?.forEach { (typeKey, typeValue) ->
            val sortedGroupValue = typeValue.sortedBy { it.value }
            val groupByValue = sortedGroupValue.groupBy { it.value }
            groupByValue.forEach { (valueKey, valueList) ->
                val temp = valueList.first()
                val items = valueList.sortedBy { it.displayOrder }
                list.add(VaccinationGroupItem(EpiGroupName.getGroupName(valueKey, typeKey), temp.scheduledDate, false, items))
            }
        }

        return list
    }

    suspend fun saveImmunisationList(
        request: RequestCreateImmunisation,
        response: MutableLiveData<Resource<ResponseCreateImmunisation>>
    ) {
        response.postLoading()
        try {
            val apiResponse = apiHelper.saveImmunisationList(request)
            if (apiResponse.isSuccessful) {
                response.postSuccess(apiResponse.body()?.entity)
            } else {
                response.postError()
            }
        } catch (e: Exception) {
            response.postError(e.message)
        }
    }

    suspend fun getImmunisationSummaryDetails(
        request: RequestImmunisationSummaryDetail,
        response: MutableLiveData<Resource<ResponseImmunisationSummaryDetails>>
    ) {
        response.postLoading()
        try {
            val apiResponse = apiHelper.getImmunisationSummaryDetails(request)
            if (apiResponse.isSuccessful) {
                response.postSuccess(apiResponse.body()?.entity)
            } else {
                response.postError()
            }
        } catch (e: Exception) {
            response.postError(e.message)
        }
    }

    suspend fun saveImmunisationSummaryDetails(
        request: RequestImmunisationSummaryCreate,
        response: MutableLiveData<Resource<ResponseImmunisationSummaryCreate>>
    ) {
        response.postLoading()
        try {
            val apiResponse = apiHelper.saveImmunisationSummaryDetails(request)
            if (apiResponse.isSuccessful) {
                response.postSuccess(apiResponse.body()?.entity)
            } else {
                response.postError()
            }
        } catch (e: Exception) {
            response.postError(e.message)
        }
    }

}