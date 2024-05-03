package com.medtroniclabs.spice.ui.medicalreview.diagnosis.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.data.DiseaseCategoryItems
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.DiagnosisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiagnosisViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: DiagnosisRepository
) : ViewModel() {

    var selectedDiseaseConditions = ArrayList<ChipViewItemModel>()
    var selectedDiseaseCategories = ArrayList<ChipViewItemModel>()
    val diagnosisList = MutableLiveData<Resource<List<DiseaseCategoryItems>>>()
    var selectedDiseaseCategoryName = ""

    fun getDiagnosisList() {
        viewModelScope.launch(dispatcherIO) {
            repository.getDiagnosisList(diagnosisList)
        }
    }
}