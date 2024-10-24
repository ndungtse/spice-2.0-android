package com.medtroniclabs.spice.ncd.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.history.HistoryEntity
import com.medtroniclabs.spice.data.history.MedicalReviewHistory
import com.medtroniclabs.spice.data.history.NCDMedicalReviewHistory
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.model.ReferralDetailRequest
import com.medtroniclabs.spice.model.ReferredDate
import com.medtroniclabs.spice.ncd.medicalreview.repo.NCDMedicalReviewRepository
import com.medtroniclabs.spice.network.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NCDMedicalReviewCMRViewModel @Inject constructor(
    private var ncdMedicalReviewRepo: NCDMedicalReviewRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    // prescription
    var prescriptionReferralDates = MutableLiveData<List<ReferredDate>>()
    val prescriptionTicketLiveData = MutableLiveData<Resource<HistoryEntity>>()
    var patientVisitId: String? = null

    // medical review History
    var medicalReferralDates = MutableLiveData<List<ReferredDate>>()
    val medicalReviewTicketLiveData = MutableLiveData<Resource<NCDMedicalReviewHistory>>()
    var medicalVisitId: String? = null

    fun getMedicalReviewHistory(patientId: String? = null, medicalVisitId: String? = null) {
        viewModelScope.launch(dispatcherIO) {
            medicalReviewTicketLiveData.postLoading()
            medicalReviewTicketLiveData.postValue(
                ncdMedicalReviewRepo.getNCDMedicalReviewHistory(
                    ReferralDetailRequest(
                        patientReference = patientId,
                        patientVisitId = medicalVisitId,
                        requestFrom = null
                    )
                )
            )
        }
    }
}

