package com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.data.model.TbHistory
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.tb.repo.TbMedicalReviewRepo
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TbSummaryViewModel @Inject constructor(
    private val tbMedicalReviewRepo: TbMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    var nextFollowupDate: String? = null
    var patientStatus: String? = null
    var treatmentOutCome: String? = null
    val tbSummary = MutableLiveData<Resource<TbHistory>>()

    private val getPatientStatusMeta = MutableLiveData<String>()
    val getPatientStatusLiveData: LiveData<List<MedicalReviewMetaItems>> =
        getPatientStatusMeta.switchMap {
            tbMedicalReviewRepo.getExaminationsComplaints(it, MedicalReviewTypeEnums.TB.name)
        }

    fun getPatientStatus(category: String) {
        getPatientStatusMeta.value = category
    }

    private val getTreatmentOutComeMeta = MutableLiveData<String>()

    val getTreatmentOutComeLiveData: LiveData<List<MedicalReviewMetaItems>> =
        getTreatmentOutComeMeta.switchMap {
            tbMedicalReviewRepo.getExaminationsComplaints(it, MedicalReviewTypeEnums.TB.name)
        }
    fun getTreatmentOutCome(category: String) {
        getTreatmentOutComeMeta.value = category
    }

    fun fetchTbAssessmentDetails(encounterId: String?, fhirId: String?) {
        viewModelScope.launch(dispatcherIO) {
            tbSummary.postLoading()
            tbSummary.postValue(
                tbMedicalReviewRepo.fetchTbAssessmentDetails(
                    MotherNeonateAncRequest(id = encounterId, patientReference = fhirId)
                )
            )
        }
    }
}