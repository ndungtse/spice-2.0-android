package com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.repo.MotherNeonateANCRepo
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.repo.MotherNeonatePNCRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotherNeonatePNCViewModel @Inject constructor(
    private val motherNeonateANCRepo: MotherNeonateANCRepo,
    private val motherNeonatePNCRepo: MotherNeonatePNCRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
):ViewModel() {
    var aliveStatus: Boolean=false
    var deliveryKit: Boolean? = null
    var aliveStatus: Boolean? = null
    val resultFlowHashMap = HashMap<String, Any>()
    var lastLocation: Location? = null
    var id: String? = null
    var pncVisit: Long = -1
    val motherNeonateMetaResponse = MutableLiveData<Resource<Boolean>>()


    var memberId: String? = null
    fun getMotherNeoNatePncStaticData() {
        viewModelScope.launch(dispatcherIO) {
            motherNeonatePNCRepo.getMotherNeoNatePncStaticData(motherNeonateMetaResponse)
        }
    }

}