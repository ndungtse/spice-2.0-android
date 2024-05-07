package com.medtroniclabs.spice.ui.medicalreview.examinations

import BreastfeedingProblem
import Examination
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.medtroniclabs.spice.data.ExaminationModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.repo.ExaminationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExaminationCardViewModel @Inject constructor(
    @IoDispatcher val dispatcherIO: CoroutineDispatcher,
    private val examinationsRepository: ExaminationsRepository
) : ViewModel() {

    private val _examinationQuestionsLiveData = MutableLiveData<ArrayList<ExaminationModel>>()
    val examinationQuestionsLiveData: LiveData<ArrayList<ExaminationModel>>
        get() = _examinationQuestionsLiveData

    var examinationResultHashMap = HashMap<String, Any>()

    fun getExaminationQuestionsByWorkFlow(workFlowType: String) {
        viewModelScope.launch(dispatcherIO) {
            val examinationListItem =
                examinationsRepository.getExaminationQuestionsByWorkFlow(workFlowType)
            examinationListItem.formInput?.let {
                val examinationModelList = Gson().fromJson(
                    it,
                    Array<ExaminationModel>::class.java
                ).asList()
                _examinationQuestionsLiveData.postValue(ArrayList(examinationModelList))
            }
        }
    }

    fun mapResultMapToExaminationModel() {

    }
}