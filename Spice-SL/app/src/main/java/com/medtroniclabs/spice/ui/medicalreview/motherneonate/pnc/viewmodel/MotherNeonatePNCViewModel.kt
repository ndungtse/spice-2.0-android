package com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.viewmodel

import MotherNeonatePncRequest
import PncChild
import PncMother
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.model.ChipViewItemModel
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.model.PatientEncounterResponse
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.repo.MotherNeonatePNCRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotherNeonatePNCViewModel @Inject constructor(
    private val motherNeonatePNCRepo: MotherNeonatePNCRepo,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    var aliveStatus: Boolean? = null
    val resultFlowHashMap = HashMap<String, Any>()
    var lastLocation: Location? = null
    var id: String? = null
    var pncVisit: Long = -1
    val motherMetaResponse = MutableLiveData<Resource<Boolean>>()
    val neonateMetaResponse = MutableLiveData<Resource<Boolean>>()
    var patientId: String? = null
    var presentingComplaints = ArrayList<ChipViewItemModel>()
    var systemicExamination = ArrayList<ChipViewItemModel>()
    var motherNeonatePncRequest: MotherNeonatePncRequest = MotherNeonatePncRequest().apply {
        pncChild = PncChild().apply { encounter = MedicalReviewEncounter() }
        pncMother = PncMother().apply { encounter = MedicalReviewEncounter() }
    }
    val pncSaveResponse = MutableLiveData<Resource<PatientEncounterResponse>>()
    var isNeonate = false
    var isSwipe = false


    var memberId: String? = null
    fun getMotherPncStaticData() {
        viewModelScope.launch(dispatcherIO) {
            motherNeonatePNCRepo.getMotherPncStaticData(motherMetaResponse)
        }
    }

    fun getNeonatePncStaticData() {
        viewModelScope.launch(dispatcherIO) {
            motherNeonatePNCRepo.getNeonatePncStaticData(neonateMetaResponse)
        }
    }

    fun saveMotherNeonatePncData() {
        viewModelScope.launch(dispatcherIO) {
            pncSaveResponse.postLoading()
            pncSaveResponse.postValue(
                motherNeonatePNCRepo.saveMotherNeonatePncData(
                    motherNeonatePncRequest
                )
            )
        }
    }

}