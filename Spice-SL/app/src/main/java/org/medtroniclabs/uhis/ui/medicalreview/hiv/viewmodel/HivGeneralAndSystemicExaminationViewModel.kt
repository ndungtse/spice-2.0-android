package org.medtroniclabs.uhis.ui.medicalreview.hiv.viewmodel

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
class HivGeneralAndSystemicExaminationViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: ExaminationComplaintsRepository,
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
