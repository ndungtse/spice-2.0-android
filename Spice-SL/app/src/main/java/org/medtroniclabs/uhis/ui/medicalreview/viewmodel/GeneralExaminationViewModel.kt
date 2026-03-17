package org.medtroniclabs.uhis.ui.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.model.ChipViewItemModel
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.repo.ExaminationComplaintsRepository
import javax.inject.Inject

@HiltViewModel
class GeneralExaminationViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private var repository: ExaminationComplaintsRepository,
) : ViewModel() {
    var systemicExaminationsType: String = ""
    var selectedSystemicExaminations = ArrayList<ChipViewItemModel>()
    var enteredExaminationNotes = ""
    val systemicExaminationList = MutableLiveData<Resource<List<MedicalReviewMetaItems>>>()
    var isMotherPnc: Boolean = false
    var breastConditionValue: String? = null
    var uterusConditionValue: String? = null
    var specifyCondition: String? = null
    var specifyConditionUterus: String? = null
    var breastFeeding: Boolean? = null
    val breastConditionMap = HashMap<String, Any>()
    val uterusConditionMap = HashMap<String, Any>()

    fun getSystemicExaminationList(type: String) {
        viewModelScope.launch(dispatcherIO) {
            systemicExaminationList.postLoading()
            systemicExaminationList.postValue(repository.getComplaintsListByType(type))
        }
    }
}
