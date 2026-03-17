package org.medtroniclabs.uhis.ui.medicalreview.abovefiveyears

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.repo.ExaminationComplaintsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PresentingComplaintsViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: ExaminationComplaintsRepository,
) : ViewModel() {
    var presentingComplaintsType: String = ""
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
