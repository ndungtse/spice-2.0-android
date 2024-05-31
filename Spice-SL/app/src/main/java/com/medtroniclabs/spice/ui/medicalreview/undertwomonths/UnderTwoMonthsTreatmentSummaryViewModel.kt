package com.medtroniclabs.spice.ui.medicalreview.undertwomonths

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.medicalreview.CreateUnderTwoMonthsResponse
import com.medtroniclabs.spice.model.medicalreview.UnderTwoMonthsSummaryDetails
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnderTwoMonthsTreatmentSummaryViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository : UnderTwoMonthsTreatmentSummaryRepository
) : ViewModel() {
    val summaryDetailsLiveData = MutableLiveData<Resource<UnderTwoMonthsSummaryDetails>>()
    var nextVisitDate: String? = null
    var selectedPatientStatus: String? = null
    val checkSubmitBtn = MutableLiveData<Boolean>()

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> get() = _isRefreshing

    fun setRefreshing(isRefreshing: Boolean) {
        _isRefreshing.value = isRefreshing
    }

    fun getUnderTwoMonthsSummaryDetails(request: CreateUnderTwoMonthsResponse) {
        viewModelScope.launch(dispatcherIO) {
            summaryDetailsLiveData.postLoading()
            summaryDetailsLiveData.postValue(
                repository.getMedicalReviewForUnderTwoMonths(
                    request
                )
            )
        }
    }

    fun setSubmitBtn() {
        checkSubmitBtn.value = true
    }
}

