package com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.model.BpAndWeightRequestModel
import com.medtroniclabs.spice.data.model.BpAndWeightResponse
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.tb.repo.TbMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BmiViewModel @Inject constructor(
    private val tbMedicalReviewRepo: TbMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    var lastLocation: Location? = null
    val getBmiList = MutableLiveData<Resource<List<BpAndWeightResponse>>>()
    fun fetchBmiList(motherNeonateAncRequest: MotherNeonateAncRequest) {
        viewModelScope.launch(dispatcherIO) {
            getBmiList.postLoading()
            getBmiList.postValue(tbMedicalReviewRepo.fetchBmiList(motherNeonateAncRequest))
        }
    }

    val saveBMI = MutableLiveData<Resource<HashMap<String, Any>>>()

    fun saveBMI(bpAndWeightRequestModel: BpAndWeightRequestModel) {
        viewModelScope.launch(dispatcherIO) {
            saveBMI.postLoading()
            saveBMI.postValue(
                tbMedicalReviewRepo.createBMI(
                    bpAndWeightRequestModel
                )
            )
        }
    }
}