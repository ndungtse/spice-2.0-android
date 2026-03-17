package org.medtroniclabs.uhis.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.ncd.assessment.repo.BloodPressureRepo
import org.medtroniclabs.uhis.ncd.assessment.repo.GlucoseRepo
import org.medtroniclabs.uhis.ncd.data.BPBGListModel
import org.medtroniclabs.uhis.ncd.data.BPLogList
import org.medtroniclabs.uhis.ncd.data.GraphModel
import org.medtroniclabs.uhis.network.resource.Resource
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
