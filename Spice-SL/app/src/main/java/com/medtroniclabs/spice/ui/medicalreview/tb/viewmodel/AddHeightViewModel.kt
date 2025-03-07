package com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.model.BpAndWeightRequestModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.tb.repo.TbMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddHeightViewModel @Inject constructor(
    private val tbMedicalReviewRepo: TbMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    val saveHeight = MutableLiveData<Resource<HashMap<String, Any>>>()
    var lastLocation: Location? = null
    fun saveHeight(bpAndWeightRequestModel: BpAndWeightRequestModel) {
        viewModelScope.launch(dispatcherIO) {
            saveHeight.postLoading()
            saveHeight.postValue(tbMedicalReviewRepo.createHeight(bpAndWeightRequestModel))
        }
    }
}