package com.medtroniclabs.spice.ui.medicalreview.labourDelivery

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.data.LabourDeliveryMetaEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.LabourDeliveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LabourDeliveryViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO : CoroutineDispatcher,
    private var repository: LabourDeliveryRepository
) : ViewModel() {

    val timeOfDeliveryMap= HashMap<String, Any>()
    val timeOfLabourOnsetMap= HashMap<String, Any>()
    val perineumStateMap= HashMap<String, Any>()
    val genderFlow = HashMap<String, Any>()
    val stateOfBaby = HashMap<String, Any>()
    val labourDeliveryMetaLiveData = MutableLiveData<Resource<Boolean>>()
    val labourDeliveryMetaList = MutableLiveData<Resource<List<LabourDeliveryMetaEntity>>>()

    fun getStaticMetaData() {
        viewModelScope.launch(dispatcherIO) {
            repository.getStaticMetaData(labourDeliveryMetaLiveData)
        }
    }

    fun getLabourDeliveryMetaList() {
        viewModelScope.launch(dispatcherIO) {
            repository.getLabourDeliveryList( labourDeliveryMetaList)
        }
    }
}