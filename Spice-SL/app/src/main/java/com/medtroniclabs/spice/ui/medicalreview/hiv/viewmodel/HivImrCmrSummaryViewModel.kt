package com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.MedicalReviewMetaItems
import com.medtroniclabs.spice.data.model.HivSummaryResponse
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.hiv.repo.HivMedicalReviewRepo
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HivImrCmrSummaryViewModel @Inject constructor(
    private val hivMedicalReviewRepo: HivMedicalReviewRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
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




    fun fetchHivSummaryDetails(encounterId: String?, fhirId: String?) {
        viewModelScope.launch(dispatcherIO) {
            hivSummary.postLoading()
            hivSummary.postValue(
                hivMedicalReviewRepo.fetchHivSummaryDetails(
                    MotherNeonateAncRequest(id = encounterId, patientReference = fhirId)
                )
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