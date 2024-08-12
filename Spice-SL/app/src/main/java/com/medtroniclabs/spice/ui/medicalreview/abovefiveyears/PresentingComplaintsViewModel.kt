package com.medtroniclabs.spice.ui.medicalreview.abovefiveyears

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
class PresentingComplaintsViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO : CoroutineDispatcher,
    private var repository: ExaminationComplaintsRepository
): ViewModel() {

    var presentingComplaintsType : String = ""
    var selectedPresentingComplaints = ArrayList<ChipViewItemModel>()
    var enteredComplaintNotes = ""
    val presentingComplaintsList = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()
    var isMotherPnc: Boolean = false

    fun getPresentingComplaintsList(type: String) {
        viewModelScope.launch(dispatcherIO) {
            presentingComplaintsList.postLoading()
            presentingComplaintsList.postValue(repository.getComplaintsListByType(type))
        }
    }
}