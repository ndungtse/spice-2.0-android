package com.medtroniclabs.spice.ui.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.PatientStatusResponse
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.repo.PatientStatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientStatusViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO : CoroutineDispatcher,
    private val patientStatusRepository: PatientStatusRepository
): ViewModel(){
    @Inject
    lateinit var connectivityManager: ConnectivityManager
    val patientStatusLiveData = MutableLiveData<Resource<PatientStatusResponse>>()
    //The below id represent backend generated ID from patient details response, its not a member generated ID
    var patientId:String? = null

    fun getPatientStatusDetails(patientDetails: PatientListRespModel, diagnosisType: String) {
        viewModelScope.launch(dispatcherIO) {
            patientStatusLiveData.postLoading()
            patientStatusLiveData.postValue(patientStatusRepository.getPatientStatusDetails(patientDetails, diagnosisType))
        }
    }
}