package com.medtroniclabs.spice.ncd.assessment.viewmodel

import androidx.lifecycle.ViewModel
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.PatientListRespModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class AssessmentReadingViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    var formTypeId: String? = null
    var patientDetails: PatientListRespModel? = null
}