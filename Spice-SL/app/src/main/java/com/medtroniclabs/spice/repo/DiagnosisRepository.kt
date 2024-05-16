package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.data.DiagnosisDiseaseModel
import com.medtroniclabs.spice.data.DiagnosisSaveUpdateRequest
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import javax.inject.Inject

class DiagnosisRepository @Inject constructor(
    private var roomHelper: RoomHelper,
    private var apiHelper: ApiHelper
) {

    suspend fun getDiagnosisList(
        diagnosisList: MutableLiveData<Resource<List<DiseaseCategoryItems>>>,
        diagnosisType: String
    ){
        try {
            diagnosisList.postLoading()
            val response = roomHelper.getDiagnosisList(diagnosisType)
            diagnosisList.postSuccess(response)
        } catch (e: Exception) {
            diagnosisList.postError()
        }
    }

    suspend fun saveUpdateDiagnosis(
        request: DiagnosisSaveUpdateRequest
    ): Resource<ArrayList<DiagnosisDiseaseModel>> {
        return try {
            val response = apiHelper.saveUpdateDiagnosis(request)
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

    suspend fun getDiagnosisDetails(
        request: CreateUnderTwoMonthsResponse
    ): Resource<ArrayList<DiagnosisDiseaseModel>> {
        return try {
            val response = apiHelper.getDiagnosisDetails(request)
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

}