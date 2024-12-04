package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.NCDPatientDiagnosisStatusRequest
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDPatientDiagnosisViewModel @Inject constructor(
    private val ncdMedicalReviewRepository: NCDMedicalReviewRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    val ncdPatientDiagnosisStatus = MutableLiveData<Resource<HashMap<String, Any>>>()

    fun ncdPatientDiagnosisStatus(request: NCDPatientDiagnosisStatusRequest) {
        viewModelScope.launch(dispatcherIO) {
            ncdPatientDiagnosisStatus.postLoading()
            ncdPatientDiagnosisStatus.postValue(
                ncdMedicalReviewRepository.ncdPatientDiagnosisStatus(
                    request
                )
            )
        }
    }
}