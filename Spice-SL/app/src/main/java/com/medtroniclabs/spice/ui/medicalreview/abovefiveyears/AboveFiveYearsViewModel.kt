package com.medtroniclabs.spice.ui.medicalreview.abovefiveyears

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.repo.AboveFiveYearsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboveFiveYearsViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: AboveFiveYearsRepository
) : ViewModel() {

    @Inject
    lateinit var connectivityManager: ConnectivityManager
    val aboveFiveYearsMetaLiveData = MutableLiveData<Resource<Boolean>>()
    val summaryDetailsLiveData = MutableLiveData<Resource<AboveFiveYearsSummaryDetails>>()
    val summaryMetaListItems = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()
    val aboveFiveYearsCreateResponse = MutableLiveData<Resource<AboveFiveYearsSummaryDetails>>()
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    var selectedPatientStatus: String? = null
    var selectedMedicalSupply: String? = null
    var selectedCostItem: String? = null
    var nextFollowupDate: String? = null
    var lastLocation: Location? = null

    fun getStaticMetaData(menuType: String) {
        viewModelScope.launch(dispatcherIO) {
            aboveFiveYearsMetaLiveData.postLoading()
            aboveFiveYearsMetaLiveData.postValue(repository.getStaticMetaData(menuType))
        }
    }

    fun createAboveFiveYearsResult(
        details: PatientListRespModel,
        selectedComplaintsExaminationsPair: Pair<List<String?>, List<String?>>,
        enteredComplaintsExaminationsClinicalNotes: Triple<String, String, String>
    ) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                aboveFiveYearsCreateResponse.postLoading()
                aboveFiveYearsCreateResponse.postValue(
                    repository.createAboveFiveYears(
                        details,
                        selectedComplaintsExaminationsPair,
                        enteredComplaintsExaminationsClinicalNotes,
                        lastLocation
                    )
                )
            }
        }
    }

    fun getSummaryListMetaItems(type: String) {
        viewModelScope.launch(dispatcherIO) {
            summaryMetaListItems.postLoading()
            summaryMetaListItems.postValue(repository.getSummaryDetailMetaItems(type))
        }
    }

    fun getAboveFiveYearsSummaryDetails(request: AboveFiveYearsSummaryRequest) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                summaryDetailsLiveData.postLoading()
                summaryDetailsLiveData.postValue(repository.getAboveFiveYearsSummaryDetails(request))
            }
        }
    }

    fun aboveFiveYearsSummaryCreate(details: PatientListRespModel, submitCreateId: String) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                summaryCreateResponse.postLoading()
                summaryCreateResponse.postValue(
                    repository.aboveFiveYearsSummaryCreate(
                        details,
                        submitCreateId,
                        selectedMedicalSupply,
                        selectedCostItem,
                        selectedPatientStatus,
                        nextFollowupDate
                    )
                )
            }
        }
    }
}