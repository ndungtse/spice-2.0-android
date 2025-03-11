package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.spice.appextensions.getLocalDate
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.ConsentFormType
import com.medtroniclabs.spice.common.EpiGroupName
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.medicalreview.EpiCatchUpPolicyItem
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
import java.time.LocalDate
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

        val historyMap = mutableMapOf<String, LocalDate?>()

        val groupByType = vaccinationList?.groupBy { it.type }
        var takenAnyOneLastSchedule = true
        groupByType?.forEach { (typeKey, typeValue) ->
            val sortedGroupValue = typeValue.sortedBy { it.value }
            val groupByValue = sortedGroupValue.groupBy { it.value }
            groupByValue.forEach { (valueKey, valueList) ->
                val temp = valueList.first()
                val items = valueList.sortedBy { it.displayOrder }
                // Logic to handle minimum 4 weeks gap for two vaccine
                items.forEach { item ->
                    if (takenAnyOneLastSchedule) {
                        val lastVaccinatedDate = historyMap[item.category]
                        val scheduledDate = item.scheduledDate.getLocalDate()

                        item.updatedScheduleDate = when {
                            item.vaccineOrder == 1 -> scheduledDate
                            lastVaccinatedDate != null && lastVaccinatedDate.isBefore(scheduledDate) -> scheduledDate
                            else -> lastVaccinatedDate
                        }

                        historyMap[item.category] = item.vaccinatedDate?.getLocalDate()?.plusWeeks(4)
                    } else {
                        item.updatedScheduleDate = null
                        historyMap[item.category] = null
                    }
                }
                takenAnyOneLastSchedule = items.any { it.vaccinatedDate != null }
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

    suspend fun getCatchUpPolicyItems(
        response: MutableLiveData<Resource<List<EpiCatchUpPolicyItem>>>
    ) {
        response.postLoading()
        roomHelper.getConsentFormByType(ConsentFormType.EPI)?.let { consentForm ->
            val responseType = object : TypeToken<List<EpiCatchUpPolicyItem>>() {}.type
            val result: MutableList<EpiCatchUpPolicyItem> = Gson().fromJson(consentForm.content, responseType)
            result.add(0, EpiCatchUpPolicyItem("","","",""))
            response.postSuccess(result)
        } ?: run {
            response.postError()
        }
    }

}