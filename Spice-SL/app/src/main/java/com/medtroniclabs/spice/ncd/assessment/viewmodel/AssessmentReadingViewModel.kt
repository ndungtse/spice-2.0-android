package com.medtroniclabs.spice.ncd.assessment.viewmodel

import androidx.lifecycle.ViewModel
import com.medtroniclabs.spice.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class AssessmentReadingViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    var formTypeId: String? = null

    var patientId: String? = null
    var relatedPersonFhirId: String? = null
    var identityValue: String? = null
}