package org.medtroniclabs.uhis.repo

import androidx.lifecycle.MutableLiveData
import org.medtroniclabs.uhis.appextensions.postError
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.appextensions.postSuccess
import org.medtroniclabs.uhis.data.DiagnosisDiseaseModel
import org.medtroniclabs.uhis.data.DiagnosisSaveUpdateRequest
import org.medtroniclabs.uhis.data.DiseaseCategoryItems
import org.medtroniclabs.uhis.db.local.RoomHelper
import org.medtroniclabs.uhis.model.medicalreview.CreateUnderTwoMonthsResponse
import org.medtroniclabs.uhis.network.ApiHelper
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.resource.ResourceState
import javax.inject.Inject

class DiagnosisRepository @Inject constructor(
    private var roomHelper: RoomHelper,
    private var apiHelper: ApiHelper,
) {
    suspend fun getDiagnosisList(
        diagnosisList: MutableLiveData<Resource<List<DiseaseCategoryItems>>>,
        diagnosisType: String,
    ) {
        try {
            diagnosisList.postLoading()
            val response = roomHelper.getDiagnosisList(diagnosisType)
            diagnosisList.postSuccess(response)
        } catch (e: Exception) {
            diagnosisList.postError()
        }
    }

    suspend fun saveUpdateDiagnosis(request: DiagnosisSaveUpdateRequest): Resource<ArrayList<DiagnosisDiseaseModel>> =
        try {
            val response = apiHelper.saveUpdateDiagnosis(request)
            if (response.isSuccessful) {
                val res = response.body()
                if (res?.status == true) {
                    Resource(state = ResourceState.SUCCESS, data = res.entity)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }

    suspend fun getDiagnosisDetails(request: CreateUnderTwoMonthsResponse): Resource<ArrayList<DiagnosisDiseaseModel>> =
        try {
            val response = apiHelper.getDiagnosisDetails(request)
            if (response.isSuccessful) {
                val res = response.body()
                if (res?.status == true) {
                    Resource(state = ResourceState.SUCCESS, data = res.entity)
                } else {
                    Resource(state = ResourceState.ERROR)
                }
            } else {
                Resource(state = ResourceState.ERROR)
            }
        } catch (e: Exception) {
            Resource(state = ResourceState.ERROR)
        }
}
