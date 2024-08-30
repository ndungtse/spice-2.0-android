package com.medtroniclabs.spice.ui.landing.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.app.analytics.db.AnalyticsRepository
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.data.UserProfile
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.boarding.repo.MetaRepository
import com.medtroniclabs.spice.ui.BaseViewModel
import com.medtroniclabs.spice.ui.boarding.repo.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val metaRepository: MetaRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    var villageListResponse = MutableLiveData<Resource<List<VillageEntity>>>()
    val menuListLiveData = MutableLiveData<Resource<List<MenuEntity>>>()
    val userProfileLiveData = MutableLiveData<Resource<UserProfile>>()
    val defaultHealthFacilityLiveData = MutableLiveData<Resource<HealthFacilityEntity?>>()
    val userHealthFacilityLiveData = MutableLiveData<Resource<ArrayList<HealthFacilityEntity>>>()

    var selectedSiteEntity: HealthFacilityEntity ?= null


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

}