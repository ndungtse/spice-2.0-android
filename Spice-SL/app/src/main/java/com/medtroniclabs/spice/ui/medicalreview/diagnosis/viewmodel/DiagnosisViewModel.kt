package com.medtroniclabs.spice.ui.medicalreview.diagnosis.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.DiagnosisDiseaseModel
import com.medtroniclabs.spice.data.DiagnosisSaveUpdateRequest
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.HivVitalsRequest
import com.medtroniclabs.spice.model.medicalreview.HivVitalsResponse
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.repo.DiagnosisRepository
import com.medtroniclabs.spice.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.cd4_percentage
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.emtctVisitStatus
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiagnosisViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: DiagnosisRepository,
    private var hivRepository: HivMedicalReviewRepo
) : ViewModel() {

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    val diagnosisMetaList = MutableLiveData<Resource<List<DiseaseCategoryItems>>>()
    val siteOfDiseaseMetaList = MutableLiveData<Resource<List<DiseaseCategoryItems>>>()
    val diagnosisDetailsList = MutableLiveData<Resource<ArrayList<DiagnosisDiseaseModel>>>()
    val diagnosisSaveUpdateResponse = MutableLiveData<Resource<ArrayList<DiagnosisDiseaseModel>>>()
    var viewDiagnosis: Boolean = true
    var diagnosisType:String = ""
    val hivVitalsDetailLiveData = MutableLiveData<Resource<HivVitalsResponse>>()

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

    fun getHivVitalsDetails(patientReference: String?, memberId: String?) {
        viewModelScope.launch(dispatcherIO) {
            hivVitalsDetailLiveData.postLoading()
            hivVitalsDetailLiveData.postValue(
                hivRepository.getHivVitalsDetails(
                    HivVitalsRequest(
                        patientReference,
                        memberId,
                        arrayListOf(
                            MedicalReviewTypeEnums.whoClinicalStage.name,
                            MedicalReviewTypeEnums.cd4.name,
                            cd4_percentage,
                            MedicalReviewTypeEnums.weight.name,
                            MedicalReviewTypeEnums.height.name,
                            emtctVisitStatus
                        )
                    )
                )
            )
        }
    }


}