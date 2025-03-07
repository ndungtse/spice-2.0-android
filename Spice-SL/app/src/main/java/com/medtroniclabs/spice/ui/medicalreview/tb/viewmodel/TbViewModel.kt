package com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.tb.repo.TbMedicalReviewRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TbViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val tbRepo: TbMedicalReviewRepo
) : ViewModel() {
    var patientId: String? = null
    var memberId: String? = null
    val tbMetaResponse = MutableLiveData<Resource<Boolean>>()

    fun getTbStaticData() {
        viewModelScope.launch(dispatcherIO) {
            tbRepo.getTbStaticData(tbMetaResponse)
        }
    }
}