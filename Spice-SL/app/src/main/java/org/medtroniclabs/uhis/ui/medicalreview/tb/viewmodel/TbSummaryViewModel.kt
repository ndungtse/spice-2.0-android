package org.medtroniclabs.uhis.ui.medicalreview.tb.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.model.MotherNeonateAncRequest
import org.medtroniclabs.uhis.data.model.TbHistory
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.medicalreview.tb.repo.TbMedicalReviewRepo
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TbSummaryViewModel @Inject constructor(
    private val tbMedicalReviewRepo: TbMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    var nextFollowupDate: String? = null
    var patientStatus: String? = null
    var treatmentOutCome: String? = null
    val tbSummary = MutableLiveData<Resource<TbHistory>>()
    private val getTreatmentOutComeMeta = MutableLiveData<String>()

    fun getTreatmentOutCome(category: String) {
        getTreatmentOutComeMeta.value = category
    }

    val getTreatmentOutComeLiveData: LiveData<List<MedicalReviewMetaItems>> =
        getTreatmentOutComeMeta.switchMap {
            tbMedicalReviewRepo.getExaminationsComplaints(it, MedicalReviewTypeEnums.TB.name)
        }

    fun fetchTbAssessmentDetails(
        encounterId: String?,
        fhirId: String?,
    ) {
        viewModelScope.launch(dispatcherIO) {
            tbSummary.postLoading()
            tbSummary.postValue(
                tbMedicalReviewRepo.fetchTbAssessmentDetails(
                    MotherNeonateAncRequest(id = encounterId, patientReference = fhirId),
                ),
            )
        }
    }
}
