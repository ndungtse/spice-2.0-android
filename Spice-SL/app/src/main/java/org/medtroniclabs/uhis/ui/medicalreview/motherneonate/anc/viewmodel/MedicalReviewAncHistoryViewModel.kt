package org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.MotherNeonateAncSummaryModel
import org.medtroniclabs.uhis.data.model.MotherNeonateAncRequest
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.repo.MotherNeonateANCRepo
import javax.inject.Inject

@HiltViewModel
class MedicalReviewAncHistoryViewModel @Inject constructor(
    private val motherNeonateANCRepo: MotherNeonateANCRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    val motherNeonateAncSummary = MutableLiveData<Resource<MotherNeonateAncSummaryModel>>()

    fun getMedicalReviewAncHistory(
        id: String?,
        fhirId: String?,
    ) {
        viewModelScope.launch(dispatcherIO) {
            motherNeonateAncSummary.postLoading()
            motherNeonateAncSummary.postValue(
                motherNeonateANCRepo.fetchSummaryDetails(
                    MotherNeonateAncRequest(id, previousHistory = true, patientReference = fhirId),
                ),
            )
        }
    }
}
