package org.medtroniclabs.uhis.ui.medicalreview.tb.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.DiagnosisDiseaseModel
import org.medtroniclabs.uhis.data.DiagnosisSaveUpdateRequest
import org.medtroniclabs.uhis.data.DiseaseCategoryItems
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.model.medicalreview.CreateUnderTwoMonthsResponse
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.utils.ConnectivityManager
import org.medtroniclabs.uhis.repo.DiagnosisRepository
import javax.inject.Inject

@HiltViewModel
class TbConfirmDiagnosisAndSiteOfDiseaseViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: DiagnosisRepository,
) : ViewModel() {
    @Inject
    lateinit var connectivityManager: ConnectivityManager

    val diagnosisMetaList = MutableLiveData<Resource<List<DiseaseCategoryItems>>>()
    val siteOfDiseaseMetaList = MutableLiveData<Resource<List<DiseaseCategoryItems>>>()
    val organAffectedMetaList = MutableLiveData<Resource<List<DiseaseCategoryItems>>>()
    val diagnosisDetailsList = MutableLiveData<Resource<ArrayList<DiagnosisDiseaseModel>>>()
    val diagnosisSaveUpdateResponse = MutableLiveData<Resource<ArrayList<DiagnosisDiseaseModel>>>()

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
