package com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.ExaminationComplaintsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HivGeneralAndSystemicExaminationViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: ExaminationComplaintsRepository
) : ViewModel() {
    var systemicExaminationsType: String = ""
    var selectedSystemicExaminations = ArrayList<ChipViewItemModel>()
    val systemicExaminationList = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()
    val resultHashMap = hashMapOf<String, String?>()
    fun getSystemicExaminationList(type: String) {
        viewModelScope.launch(dispatcherIO) {
            systemicExaminationList.postLoading()
            systemicExaminationList.postValue(repository.getComplaintsListByType(type))
        }
    }
}