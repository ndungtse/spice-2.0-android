package com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.MotherNeonateAncSummaryModel
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.repo.MotherNeonateANCRepo
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MotherNeonateSummaryViewModel @Inject constructor(
    private val motherNeonateANCRepo: MotherNeonateANCRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    var nextFollowupDate: String? = null
    var patientStatus: String? = null
    private val getAncMetaForPatientStatus = MutableLiveData<String>()
    val motherNeonateAncSummary = MutableLiveData<Resource<MotherNeonateAncSummaryModel>>()
    val checkSubmitBtn = MutableLiveData<Boolean>()

    val ancMetaLiveDataForPatientStatus: LiveData<List<MedicalReviewMetaItems>> =
        getAncMetaForPatientStatus.switchMap {
            motherNeonateANCRepo.getExaminationsComplaintsForAnc(it, MedicalReviewTypeEnums.ANC.name)
        }

    fun setAncReqToGetMetaForPatientStatus(category: String) {
        getAncMetaForPatientStatus.value = category
    }

    fun checkSubmitBtn() {
        checkSubmitBtn.value = true
    }

    fun fetchMotherNeonateSummary(encounterId: String?,fhirId: String?) {
        viewModelScope.launch(dispatcherIO) {
            motherNeonateAncSummary.postLoading()
            motherNeonateAncSummary.postValue(
                motherNeonateANCRepo.fetchSummary(
                    MotherNeonateAncRequest(id = encounterId, patientReference = fhirId)
                )
            )
        }
    }
}