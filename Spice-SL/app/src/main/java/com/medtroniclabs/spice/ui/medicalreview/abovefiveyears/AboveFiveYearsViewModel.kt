package com.medtroniclabs.spice.ui.medicalreview.abovefiveyears

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryDetails
import com.medtroniclabs.spice.data.AboveFiveYearsSummaryRequest
import com.medtroniclabs.spice.data.AboveFiveYearsSummarySubmitRequest
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.AboveFiveYearsSubmitRequest
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.utils.ConnectivityManager
import com.medtroniclabs.spice.repo.AboveFiveYearsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboveFiveYearsViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO : CoroutineDispatcher,
    private var repository: AboveFiveYearsRepository
): ViewModel(){

    @Inject
    lateinit var connectivityManager: ConnectivityManager
    val aboveFiveYearsMetaLiveData = MutableLiveData<Resource<Boolean>>()
    val summaryDetailsLiveData = MutableLiveData<Resource<AboveFiveYearsSummaryDetails>>()
    val summaryMetaListItems = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()
    val aboveFiveYearsCreateResponse = MutableLiveData<Resource<AboveFiveYearsSummaryDetails>>()
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String,Any>>>()
    var selectedPatientStatus: String? = null
    var selectedMedicalSupply: String? = null
    var selectedCostItem: String? = null
    var nextFollowupDate: String? = null

    fun getStaticMetaData(menuType: String) {
        viewModelScope.launch(dispatcherIO){
            repository.getStaticMetaData(aboveFiveYearsMetaLiveData, menuType)
        }
    }

    fun createAboveFiveYearsResult(request: AboveFiveYearsSubmitRequest){
        if (connectivityManager.isNetworkAvailable()){
            viewModelScope.launch(dispatcherIO) {
                repository.createAboveFiveYears(request, aboveFiveYearsCreateResponse)
            }
        }
    }

    fun getSummaryListMetaItems(type: String) {
        viewModelScope.launch(dispatcherIO) {
            repository.getSummaryDetailMetaItems(type, summaryMetaListItems)
        }
    }

    fun getAboveFiveYearsSummaryDetails(request: AboveFiveYearsSummaryRequest){
        if (connectivityManager.isNetworkAvailable()){
            viewModelScope.launch(dispatcherIO) {
                repository.getAboveFiveYearsSummaryDetails(request, summaryDetailsLiveData)
            }
        }
    }

    fun aboveFiveYearsSummaryCreate(request: AboveFiveYearsSummarySubmitRequest){
        if (connectivityManager.isNetworkAvailable()){
            viewModelScope.launch(dispatcherIO) {
                repository.aboveFiveYearsSummaryCreate(request, summaryCreateResponse)
            }
        }
    }
}