package com.medtroniclabs.spice.ui.medicalreview.abovefiveyears

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.data.ExaminationsComplaintItems
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.AboveFiveYearsRepository
import com.medtroniclabs.spice.repo.ExaminationComplaintsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExaminationsComplaintsViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO : CoroutineDispatcher,
    private var repository: ExaminationComplaintsRepository
): ViewModel() {

    var presentingComplaintsType : String = ""
    var systemicExaminationsType : String = ""
    val examinationsComplaintsList = MutableLiveData<Resource<List<ExaminationsComplaintItems>>>()
    var selectedPresentingComplaints = ArrayList<ChipViewItemModel>()
    var selectedSystemicExaminations = ArrayList<ChipViewItemModel>()
    var enteredClinicalNotes = ""
    var enteredExaminationNotes = ""
    var enteredComplaintNotes = ""

    fun getComplaintsList(type: String) {
        viewModelScope.launch(dispatcherIO) {
            repository.getComplaintsListByType(type, examinationsComplaintsList)
        }
    }
}