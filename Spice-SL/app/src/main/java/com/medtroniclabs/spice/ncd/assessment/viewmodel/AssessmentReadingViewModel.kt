package com.medtroniclabs.spice.ncd.assessment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.assessment.repo.AssessmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class AssessmentReadingViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val assessmentRepository: AssessmentRepository
) : ViewModel() {
    var formTypeId: String? = null

    var patientId: String? = null
    var relatedPersonFhirId: String? = null

    val getFormData = MutableLiveData<String>()
    val formLayoutsLiveData: LiveData<String> = getFormData.switchMap {
        assessmentRepository.getFormData(it)
    }

    fun getFormData(formType: String) {
        getFormData.value = formType
    }
}