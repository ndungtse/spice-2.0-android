package com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.DiagnosisDiseaseModel
import com.medtroniclabs.spice.data.DiagnosisSaveUpdateRequest
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.repo.DiagnosisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TbConfirmDiagnosisAndSiteOfDiseaseViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: DiagnosisRepository
) : ViewModel() {

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    val diagnosisMetaList = MutableLiveData<Resource<List<DiseaseCategoryItems>>>()
    val siteOfDiseaseMetaList = MutableLiveData<Resource<List<DiseaseCategoryItems>>>()
    val organAffectedMetaList = MutableLiveData<Resource<List<DiseaseCategoryItems>>>()
    val diagnosisDetailsList = MutableLiveData<Resource<ArrayList<DiagnosisDiseaseModel>>>()
    val diagnosisSaveUpdateResponse = MutableLiveData<Resource<ArrayList<DiagnosisDiseaseModel>>>()
    var diagnosisType:String = ""

    fun getDiagnosisMetaList(diagnosisType: String) {
        viewModelScope.launch(dispatcherIO) {
            repository.getDiagnosisList(diagnosisMetaList, diagnosisType)
        }
    }

    fun getSiteOfDiseaseMetaList(siteOfDiseaseType: String) {
        viewModelScope.launch(dispatcherIO) {
            repository.getDiagnosisList(siteOfDiseaseMetaList, siteOfDiseaseType)
        }
    }

    fun diagnosisCreate(request: DiagnosisSaveUpdateRequest) {
        viewModelScope.launch(dispatcherIO) {
            diagnosisSaveUpdateResponse.postLoading()
            diagnosisSaveUpdateResponse.postValue(repository.saveUpdateDiagnosis(request))
        }
    }

    fun getDiagnosisDetails(request: CreateUnderTwoMonthsResponse) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                diagnosisDetailsList.postLoading()
                diagnosisDetailsList.postValue(repository.getDiagnosisDetails(request))
            }
        }
    }

    fun getOrganAffectedMetaList(organAffected: String) {
        viewModelScope.launch(dispatcherIO) {
            repository.getDiagnosisList(organAffectedMetaList, organAffected)
        }
    }
}