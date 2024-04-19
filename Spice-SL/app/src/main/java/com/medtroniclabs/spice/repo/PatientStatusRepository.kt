package com.medtroniclabs.spice.repo

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.PatientStatusResponse
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

class PatientStatusRepository @Inject constructor(
    private var apiHelper: ApiHelper
) {
    suspend fun getPatientStatus(
        patientId: String,
        patientStatusLiveData: MutableLiveData<Resource<ArrayList<PatientStatusResponse>>>
    ) {
        try {
            patientStatusLiveData.postLoading()
            val response = apiHelper.getPatientStatus(AboveFiveYearsSummaryRequest(id = patientId, type = MedicalReviewTypeEnums.medicalReview.name))
            if (response.isSuccessful) {
                response.body()?.entity?.let {
                    patientStatusLiveData.postSuccess(it)
                }
            } else {
                patientStatusLiveData.postError()
            }
        } catch (e: Exception) {
            patientStatusLiveData.postError()
        }
    }
}