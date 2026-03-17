package org.medtroniclabs.uhis.ncd.assessment.viewmodel

import androidx.lifecycle.ViewModel
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.model.PatientListRespModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class AssessmentReadingViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    var formTypeId: String? = null
    var patientDetails: PatientListRespModel? = null
}
