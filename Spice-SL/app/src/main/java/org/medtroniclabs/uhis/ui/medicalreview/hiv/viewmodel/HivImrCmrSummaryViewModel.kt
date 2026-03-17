package org.medtroniclabs.uhis.ui.medicalreview.hiv.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import org.medtroniclabs.uhis.appextensions.postLoading
import org.medtroniclabs.uhis.data.MedicalReviewMetaItems
import org.medtroniclabs.uhis.data.model.HivSummaryResponse
import org.medtroniclabs.uhis.data.model.MotherNeonateAncRequest
import org.medtroniclabs.uhis.di.IoDispatcher
import org.medtroniclabs.uhis.network.resource.Resource
import org.medtroniclabs.uhis.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HivImrCmrSummaryViewModel @Inject constructor(
    private val hivMedicalReviewRepo: HivMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) : ViewModel() {
    var nextFollowupDate: String? = null
    val hivSummary = MutableLiveData<Resource<HivSummaryResponse>>()
    var patientStatus: String? = null
    var eMTCTStatus: String? = null
    var maternalOutcome: String? = null
    val getEmtctStatusMeta = MutableLiveData<String>()
    val getMaternalOutcomeMeta = MutableLiveData<String>()
    var statusSpinner = ArrayList<Map<String, Any>>()
    var maternalOutcomeMap = ArrayList<Map<String, Any>>()
    var emtctStatusDefault: String? = null

    fun fetchHivSummaryDetails(
        encounterId: String?,
        fhirId: String?,
    ) {
        viewModelScope.launch(dispatcherIO) {
            hivSummary.postLoading()
            hivSummary.postValue(
                hivMedicalReviewRepo.fetchHivSummaryDetails(
                    MotherNeonateAncRequest(id = encounterId, patientReference = fhirId),
                ),
            )
        }
    }

    val hivEmtctStatusLiveData: LiveData<List<MedicalReviewMetaItems>> =
        getEmtctStatusMeta.switchMap {
            hivMedicalReviewRepo.getHivPatientStatus(it, MedicalReviewTypeEnums.HIV.name)
        }

    fun getEmtctStatusByCategory(category: String) {
        getEmtctStatusMeta.value = category
    }

    val hivMaternalStatusLiveData: LiveData<List<MedicalReviewMetaItems>> =
        getMaternalOutcomeMeta.switchMap {
            hivMedicalReviewRepo.getHivPatientStatus(it, MedicalReviewTypeEnums.HIV.name)
        }

    fun getMaternalStatusByCategory(category: String) {
        getMaternalOutcomeMeta.value = category
    }
}
