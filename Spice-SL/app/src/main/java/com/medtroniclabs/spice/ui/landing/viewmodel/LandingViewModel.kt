package com.medtroniclabs.spice.ui.landing.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.UserProfile
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferNotificationCountRequest
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferNotificationCountResponse
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.boarding.repo.MetaRepository
import com.medtroniclabs.spice.ui.BaseViewModel
import com.medtroniclabs.spice.ncd.data.PatientTransferListResponse
import com.medtroniclabs.spice.ncd.data.NCDPatientTransferUpdateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val metaRepository: MetaRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher
) : BaseViewModel(dispatcherIO) {

    var villageListResponse = MutableLiveData<Resource<List<VillageEntity>>>()
    val menuListLiveData = MutableLiveData<Resource<List<MenuEntity>>>()
    val userProfileLiveData = MutableLiveData<Resource<UserProfile>>()
    val defaultHealthFacilityLiveData = MutableLiveData<Resource<HealthFacilityEntity?>>()
    val userHealthFacilityLiveData = MutableLiveData<Resource<ArrayList<HealthFacilityEntity>>>()
    val patientListResponse = MutableLiveData<Resource<PatientTransferListResponse>>()
    val patientTransferNotificationCountResponse =
        MutableLiveData<Resource<NCDPatientTransferNotificationCountResponse>>()

    var selectedSiteEntity: HealthFacilityEntity ?= null
    val patientUpdateResponse = MutableLiveData<Resource<String>>()
    var transferPatientViewId: Long? = null


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

    fun getAllVillagesName() {
        viewModelScope.launch(dispatcherIO) {
            villageListResponse.postLoading()
            villageListResponse.postValue(metaRepository.getAllVillagesName())
        }
    }

    fun getDefaultHealthFacility() {
        viewModelScope.launch(dispatcherIO) {
            defaultHealthFacilityLiveData.postLoading()
            defaultHealthFacilityLiveData.postValue(metaRepository.getDefaultHealthFacility())
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

}