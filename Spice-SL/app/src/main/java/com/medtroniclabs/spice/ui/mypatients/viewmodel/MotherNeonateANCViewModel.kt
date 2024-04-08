package com.medtroniclabs.spice.ui.mypatients.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.data.ExaminationsComplaintItems
import com.medtroniclabs.spice.data.PregnancyDetailsModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.mypatients.repo.MotherNeonateANCRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotherNeonateANCViewModel @Inject constructor(
    private val motherNeonateANCRepo: MotherNeonateANCRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    val resultFlowHashMap = HashMap<String, Any>()
    val motherNeonateMetaResponse = MutableLiveData<Resource<Boolean>>()
    var selectedBloodGroup: String? = null
    val getAncMetaForPregnancyHistory = MutableLiveData<String>()
    val getAncMetaForBloodGroup = MutableLiveData<String>()
    val ancMetaLiveDataForPregnancyHistory: LiveData<List<ExaminationsComplaintItems>> = getAncMetaForPregnancyHistory.switchMap {
        motherNeonateANCRepo.getExaminationsComplaintsForAnc(it)
    }
    val ancMetaLiveDataForBloodGroup: LiveData<List<ExaminationsComplaintItems>> =
        getAncMetaForBloodGroup.switchMap {
            motherNeonateANCRepo.getExaminationsComplaintsForAnc(it)
        }
    var pregnancyDetailsModel = PregnancyDetailsModel()
    fun getMotherNeoNateAncStaticData() {
        viewModelScope.launch(dispatcherIO) {
            motherNeonateANCRepo.getMotherNeoNateAncStaticData(motherNeonateMetaResponse)
        }
    }

    fun setAncReqToGetMetaForPregnancyHistory(category: String) {
        getAncMetaForPregnancyHistory.value = category
    }

    fun setAncReqToGetMetaForBloodGroup(category: String){
        getAncMetaForBloodGroup.value = category
    }
}