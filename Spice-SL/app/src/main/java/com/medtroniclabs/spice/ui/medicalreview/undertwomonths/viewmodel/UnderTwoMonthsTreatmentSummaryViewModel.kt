package com.medtroniclabs.spice.ui.medicalreview.undertwomonths.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.SummarySubmitRequest
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.SummaryDetails
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.repo.UnderTwoMonthsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class UnderTwoMonthsTreatmentSummaryViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository : UnderTwoMonthsRepository
) : ViewModel() {
    val summaryDetailsLiveData = MutableLiveData<Resource<SummaryDetails>>()
    var nextVisitDate: String? = null
    var selectedPatientStatus: String? = null
    val checkSubmitBtn = MutableLiveData<Boolean>()
    val summaryMetaListItems = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()

    fun getUnderTwoMonthsSummaryDetails(request: CreateUnderTwoMonthsResponse) {
        summaryDetailsLiveData.postValue(Resource(state = ResourceState.SUCCESS, data = null))// Clear and post loading state
        viewModelScope.launch(dispatcherIO) {
                  summaryDetailsLiveData.postLoading()
                  summaryDetailsLiveData.postValue(
                      repository.getMedicalReviewForUnderTwoMonths(
                          request
                      )
                  )

          }
    }
    fun getSummaryListMetaItems(type: String) {
        viewModelScope.launch(dispatcherIO) {
            summaryMetaListItems.postLoading()
            summaryMetaListItems.postValue(repository.getSummaryDetailMetaItems(type))
        }
    }

    fun setSubmitBtn() {
        checkSubmitBtn.value = true
    }
}

