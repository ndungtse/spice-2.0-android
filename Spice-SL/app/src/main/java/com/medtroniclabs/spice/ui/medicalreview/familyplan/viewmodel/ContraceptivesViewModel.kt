package com.medtroniclabs.spice.ui.medicalreview.familyplan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContraceptivesViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
):ViewModel(){
    var selectedIUCD = ArrayList<ChipViewItemModel>()
    val resultHashMap = HashMap<String, Any>()
    var quantity: String =""

    val _IUCDList = MutableLiveData<ArrayList<ChipViewItemModel>>()
    val IUCDList: LiveData<ArrayList<ChipViewItemModel>>
        get() = _IUCDList

    fun getIUCDList() {
        viewModelScope.launch(dispatcherIO) {

        }
    }

    val _maritalStatus = MutableLiveData<List<String>>()
    val maritalStatus: LiveData<List<String>>
    get() = _maritalStatus
    fun getMaritalStatus() {
        viewModelScope.launch(dispatcherIO) {

        }
    }

}