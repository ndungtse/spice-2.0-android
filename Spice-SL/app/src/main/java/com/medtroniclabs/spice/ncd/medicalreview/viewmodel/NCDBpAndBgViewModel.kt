package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.assessment.repo.BloodPressureRepo
import com.medtroniclabs.spice.ncd.assessment.repo.GlucoseRepo
import com.medtroniclabs.spice.ncd.data.BPBGListModel
import com.medtroniclabs.spice.ncd.data.BPLogList
import com.medtroniclabs.spice.ncd.data.GraphModel
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDBpAndBgViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val bloodPressureRepo: BloodPressureRepo,
    private val glucoseRepo: GlucoseRepo,
) : ViewModel() {
    var totalBPTotalCount: Int? = null
    var selectedBGDropDown = MutableLiveData<Int>()
    var latestBpLogResponse: BPLogList? = null
    var totalBGCount: Int = 0
    var totalBPCount: Int = 0
    var bpLogListResponseLiveData = MutableLiveData<Resource<BPBGListModel>>()
    var glucoseLogListResponseLiveData = MutableLiveData<Resource<BPBGListModel>>()
    val onBPValueSelectedObserver = MutableLiveData<GraphModel>()
    val onBGValueSelectedObserver = MutableLiveData<GraphModel>()

    fun bpLogList(request: BPBGListModel) {
        viewModelScope.launch(dispatcherIO) {
            bpLogListResponseLiveData.postLoading()
            bpLogListResponseLiveData.postValue(bloodPressureRepo.bpLogList(request))
        }
    }

    fun glucoseLogList(
        request: BPBGListModel,
        forward: Boolean? = null,
    ) {
        viewModelScope.launch(dispatcherIO) {
            glucoseLogListResponseLiveData.postLoading()
            glucoseLogListResponseLiveData.postValue(glucoseRepo.glucoseLogList(request))
        }
    }
}
