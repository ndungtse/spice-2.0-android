package com.medtroniclabs.spice.ui.landing.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.UserProfile
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferNotificationCountRequest
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferNotificationCountResponse
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferUpdateRequest
import com.medtroniclabs.spice.ncd.data.NCDSupportRequest
import com.medtroniclabs.spice.ncd.data.PatientTransferListResponse
import com.medtroniclabs.spice.network.SingleLiveEvent
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseViewModel
import com.medtroniclabs.spice.ui.boarding.repo.MetaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val metaRepository: MetaRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher
) : BaseViewModel(dispatcherIO) {

    val menuListLiveData = MutableLiveData<Resource<List<MenuEntity>>>()
    val userProfileLiveData = MutableLiveData<Resource<UserProfile>>()
    val userHealthFacilityLiveData = MutableLiveData<Resource<ArrayList<HealthFacilityEntity>>>()
    val patientListResponse = MutableLiveData<Resource<PatientTransferListResponse>>()
    val patientTransferNotificationCountResponse =
        MutableLiveData<Resource<NCDPatientTransferNotificationCountResponse>>()

    var selectedSiteEntity: HealthFacilityEntity ?= null
    val patientUpdateResponse = SingleLiveEvent<Resource<String>>()
    var transferPatientViewId: Long? = null
    var enteredSupportReason: String? =null
    private val supportResponseMutableLiveData = SingleLiveEvent<Resource<String>>()
    val supportResponseLiveData: LiveData<Resource<String>>
        get() = supportResponseMutableLiveData

    var isSupport = false
    fun getMenus() {
        viewModelScope.launch(dispatcherIO) {
            menuListLiveData.postLoading()
            menuListLiveData.postValue(metaRepository.getMenu())
        }
    }

    fun getUserProfile() {
        viewModelScope.launch(dispatcherIO) {
            userProfileLiveData.postLoading()
            userProfileLiveData.postValue(metaRepository.getUserProfile())
        }
    }

    fun getUserHealthFacility() {
        viewModelScope.launch(dispatcherIO) {
            userHealthFacilityLiveData.postLoading()
            userHealthFacilityLiveData.postValue(metaRepository.getUserHealthFacility())
        }
    }

    fun getPatientListTransfer(request: NCDPatientTransferNotificationCountRequest) {
        viewModelScope.launch(dispatcherIO) {
            patientListResponse.postLoading()
            patientListResponse.postValue(metaRepository.getPatientListTransfer(request))
        }
    }

    fun patientTransferNotificationCount(request: NCDPatientTransferNotificationCountRequest) {
        viewModelScope.launch(dispatcherIO) {
            patientTransferNotificationCountResponse.postLoading()
            patientTransferNotificationCountResponse.postValue(metaRepository.patientTransferNotificationCount(request))
        }
    }

    fun patientTransferUpdate(request: NCDPatientTransferUpdateRequest) {
        viewModelScope.launch(dispatcherIO) {
            patientUpdateResponse.postLoading()
            patientUpdateResponse.postValue(metaRepository.patientTransferUpdate(request))
        }
    }

    fun createSupportRequest(request: NCDSupportRequest) {
        viewModelScope.launch(dispatcherIO) {
            supportResponseMutableLiveData.postLoading()
            patientUpdateResponse.postValue(metaRepository.createSupportRequest(request))
        }
    }

}