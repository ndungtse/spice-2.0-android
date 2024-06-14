package com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.convertToUtcDateTime
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.data.AboveFiveYearsSummarySubmitRequest
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.data.model.PatientEncounterResponse
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.repo.MotherNeonateANCRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotherNeonateANCViewModel @Inject constructor(
    private val motherNeonateANCRepo: MotherNeonateANCRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    val motherNeonateMetaResponse = MutableLiveData<Resource<Boolean>>()
    val motherNeonateCreateResponse = MutableLiveData<Resource<PatientEncounterResponse>>()
    var motherNeonateAncRequest: MotherNeonateAncRequest = MotherNeonateAncRequest()
    val summaryCreateResponse = MutableLiveData<Resource<HashMap<String,Any>>>()
    var ancVisit: Int = -1
    var patientId: String? = null
    var memberId: String? = null
    var lastLocation: Location? = null


    fun getMotherNeoNateAncStaticData() {
        viewModelScope.launch(dispatcherIO) {
            motherNeonateANCRepo.getMotherNeoNateAncStaticData(motherNeonateMetaResponse)
        }
    }

    fun createMotherNeonate() {
        viewModelScope.launch(dispatcherIO) {
            try {
                motherNeonateAncRequest.apply {
                    encounter = MedicalReviewEncounter(
                        patientId = this@MotherNeonateANCViewModel.patientId,
                        provenance = ProvanceDto(
                            createdDateTime = System.currentTimeMillis().convertToUtcDateTime()
                        ),
                        latitude = lastLocation?.latitude ?: 0.0,
                        longitude = lastLocation?.longitude ?: 0.0,
                        startTime = DateUtils.getCurrentDateAndTime(
                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                        ),
                        endTime = DateUtils.getCurrentDateAndTime(
                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                        ),
                        referred = true,
                        visitNumber = ancVisit
                    )
                }
                motherNeonateCreateResponse.postLoading()
                motherNeonateCreateResponse.postValue(
                    motherNeonateANCRepo.saveMotherNeonateAnc(
                        motherNeonateAncRequest
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun motherNeonateSummaryCreate(request: AboveFiveYearsSummarySubmitRequest){
        viewModelScope.launch(dispatcherIO) {
            summaryCreateResponse.postLoading()
            summaryCreateResponse.postValue(motherNeonateANCRepo.motherNeonateSummaryCreate(request))
        }
    }

    fun getSubmitCreateId(): String? {
        return motherNeonateCreateResponse.value?.data?.encounterId
    }

    fun getPatientReference(): String? {
        return motherNeonateCreateResponse.value?.data?.patientReference
    }
}
