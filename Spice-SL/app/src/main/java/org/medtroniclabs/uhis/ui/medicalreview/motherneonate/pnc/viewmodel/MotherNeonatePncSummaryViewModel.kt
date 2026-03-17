package org.medtroniclabs.uhis.ui.medicalreview.motherneonate.pnc.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.MotherNeonatePncSummaryRequest
import org.medtroniclabs.uhis.data.MotherNeonatePncSummaryResponse
import org.medtroniclabs.uhis.data.history.PatientStatus
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.pnc.repo.MotherNeonatePNCRepo
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotherNeonatePncSummaryViewModel @Inject constructor(
    private val motherNeonatePNCRepo: MotherNeonatePNCRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    var nextFollowupDate: String? = null
    var patientStatusMother: String? = null
    var patientStatusChild: String? = null
    private val getPncMetaForPatientStatus = MutableLiveData<String?>(null)
    var motherNeonatePncSummaryRequest = MotherNeonatePncSummaryRequest()
    val pncSummaryResponse = MutableLiveData<Resource<MotherNeonatePncSummaryResponse>>()
    var pncMotherPatientStatus: List<PatientStatus>? = null
    var pncChildPatientStatus: List<MedicalReviewMetaItems>? = null
    var motherNeonateAlive = true

    val pncMetaLiveDataForPatientStatus: LiveData<List<MedicalReviewMetaItems>> =
        getPncMetaForPatientStatus
            .switchMap { category ->
                category?.let {
                    motherNeonatePNCRepo.getExaminationsComplaintsForPnc(
                        MedicalReviewTypeEnums.patient_status.name,
                        it,
                    )
                }
            }

    fun setPncReqToGetMetaForPatientStatus(category: String) {
        getPncMetaForPatientStatus.value = category
    }

    fun getPncSummaryDetails() {
        viewModelScope.launch(dispatcherIO) {
            pncSummaryResponse.postLoading()
            pncSummaryResponse.postValue(
                motherNeonatePNCRepo.getPncSummaryDetails(motherNeonatePncSummaryRequest),
            )
        }
    }
}
