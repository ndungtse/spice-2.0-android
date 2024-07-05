package com.medtroniclabs.spice.ui.medicalreview.underfiveyears

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.SummaryDetails
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnderFiveYearTreatmentSummaryViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: UnderFiveYearsTreatmentSummaryRepository
) : ViewModel() {
    val summaryDetailsLiveData = MutableLiveData<Resource<SummaryDetails>>()
    var nextVisitDate: String? = null
    val checkSubmitBtn = MutableLiveData<Boolean>()
    var selectedPatientStatus: String? = null
    private val getMetaPatientStatus = MutableLiveData<String>()

    val metaLiveDataPatientStatus: LiveData<List<MedicalReviewMetaItems>> =
        getMetaPatientStatus.switchMap {
            repository.getExaminationsComplaints(
                it,
                MedicalReviewTypeEnums.UnderFiveYears.name
            )
        }

    fun getUnderFiveYearsSummaryDetails(request: CreateUnderTwoMonthsResponse) {
        viewModelScope.launch(dispatcherIO) {
            summaryDetailsLiveData.postLoading()
            summaryDetailsLiveData.postValue(
                repository.getUnderFiveYearsSummaryDetails(
                    request
                )
            )
        }
    }

    fun setMetaPatientStatus(category: String) {
        getMetaPatientStatus.value = category
    }

}