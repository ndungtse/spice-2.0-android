package com.medtroniclabs.spice.ui.landing.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.data.UserProfile
import com.medtroniclabs.spice.db.entity.HealthFacilityEntity
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.boarding.repo.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    var villageListResponse = MutableLiveData<Resource<List<VillageEntity>>>()
    val menuListLiveData = MutableLiveData<Resource<List<MenuEntity>>>()
    val userProfileLiveData = MutableLiveData<Resource<UserProfile>>()
    val defaultHealthFacilityLiveData = MutableLiveData<Resource<HealthFacilityEntity?>>()

    fun getMenus() {
        viewModelScope.launch(dispatcherIO) {
            loginRepository.getMenu(menuListLiveData)
        }
    }

    fun getUserProfile() {
        viewModelScope.launch(dispatcherIO) {
            loginRepository.getUserProfile(userProfileLiveData)
        }
    }

    fun getAllVillagesName() {
        viewModelScope.launch(dispatcherIO) {
            loginRepository.getAllVillagesName(villageListResponse)
        }
    }

    fun getDefaultHealthFacility() {
        viewModelScope.launch(dispatcherIO) {
            loginRepository.getDefaultHealthFacility(defaultHealthFacilityLiveData)
        }
    }
}