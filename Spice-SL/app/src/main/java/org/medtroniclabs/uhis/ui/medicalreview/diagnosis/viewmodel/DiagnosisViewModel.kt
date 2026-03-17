package org.medtroniclabs.uhis.ui.medicalreview.diagnosis.viewmodel

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
import org.medtroniclabs.uhis.model.medicalreview.HivVitalsRequest
import org.medtroniclabs.uhis.model.medicalreview.HivVitalsResponse
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.network.utils.ConnectivityManager
import org.medtroniclabs.uhis.repo.DiagnosisRepository
import org.medtroniclabs.uhis.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewDefinedParams.cd4_percentage
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewDefinedParams.emtctVisitStatus
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import javax.inject.Inject

@HiltViewModel
class DiagnosisViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: DiagnosisRepository,
    private var hivRepository: HivMedicalReviewRepo,
) : ViewModel() {
    @Inject
    lateinit var connectivityManager: ConnectivityManager

    val diagnosisMetaList = MutableLiveData<Resource<List<DiseaseCategoryItems>>>()
    val siteOfDiseaseMetaList = MutableLiveData<Resource<List<DiseaseCategoryItems>>>()
    val diagnosisDetailsList = MutableLiveData<Resource<ArrayList<DiagnosisDiseaseModel>>>()
    val diagnosisSaveUpdateResponse = MutableLiveData<Resource<ArrayList<DiagnosisDiseaseModel>>>()
    var viewDiagnosis: Boolean = true
    var diagnosisType: String = ""
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

    fun getHivVitalsDetails(
        patientReference: String?,
        memberId: String?,
    ) {
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
                            emtctVisitStatus,
                        ),
                    ),
                ),
            )
        }
    }
}
