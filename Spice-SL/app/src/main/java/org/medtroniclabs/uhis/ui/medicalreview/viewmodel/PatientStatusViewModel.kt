package org.medtroniclabs.uhis.ui.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.PatientStatusResponse
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.model.PatientListRespModel
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.utils.ConnectivityManager
import org.medtroniclabs.uhis.repo.PatientStatusRepository
import javax.inject.Inject

@HiltViewModel
class PatientStatusViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val patientStatusRepository: PatientStatusRepository,
) : ViewModel() {
    @Inject
    lateinit var connectivityManager: ConnectivityManager
    val patientStatusLiveData = MutableLiveData<Resource<PatientStatusResponse>>()

    // The below id represent backend generated ID from patient details response, its not a member generated ID
    var patientId: String? = null

    fun getPatientStatusDetails(
        patientDetails: PatientListRespModel,
        diagnosisType: String,
    ) {
        viewModelScope.launch(dispatcherIO) {
            patientStatusLiveData.postLoading()
            patientStatusLiveData.postValue(patientStatusRepository.getPatientStatusDetails(patientDetails, diagnosisType))
        }
    }
}
