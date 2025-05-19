package com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.HivStatus
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HIVStatusViewModel @Inject constructor(
    private val hivMedicalReviewRepo: HivMedicalReviewRepo,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
) : BaseViewModel(dispatcherIO) {

    val resultPregnantStatus = HashMap<String, Any>()
    val resultAHD = HashMap<String, Any>()
    val resultDSD = HashMap<String, Any>()
    val getHivStatusMetaList = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()
    var selectModel: String? = null
    val request: HivStatus? = null

    fun getHivStatusMeta(type: String) {
        viewModelScope.launch(dispatcherIO) {
            getHivStatusMetaList.postLoading()
            getHivStatusMetaList.postValue(hivMedicalReviewRepo.getComplaintsListByType(type))
        }
    }
}