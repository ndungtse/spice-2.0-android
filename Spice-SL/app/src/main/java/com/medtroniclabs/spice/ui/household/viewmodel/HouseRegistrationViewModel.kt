package com.medtroniclabs.spice.ui.household.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.villageId
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.HouseHoldRepository
import com.medtroniclabs.spice.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HouseRegistrationViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val houseHoldRepository: HouseHoldRepository,
) : BaseViewModel(dispatcherIO) {

    var houseHoldRegistrationLiveData = MutableLiveData<Resource<Long>>()
    var isMemberRegistration: Boolean = false
    var householdEntityDetail: HouseholdEntity? = null
    val formLayoutsLiveData = MutableLiveData<Resource<String>>()
    var houseHoldUpdateLiveData = MutableLiveData<Resource<Long>>()
    val houseHoldDetailLiveData = MutableLiveData<Resource<HouseholdEntity>>()
    var householdId: Long = -1L
    var villageListResponse = MutableLiveData<Resource<LocalSpinnerResponse>>()
    var memberID: Long = -1L
    private var lastLocation: Location? = null
    var addNewMember: Boolean = false


    var signatureFilename: String? = null
    var initialValue: String? = null


    fun getFormData(formType: String) {
        viewModelScope.launch(dispatcherIO) {
            formLayoutsLiveData.postLoading()
            formLayoutsLiveData.postValue(houseHoldRepository.getFormData(formType))
        }
    }

    fun loadDataCacheByType(type: String, tag: String) {
        viewModelScope.launch(dispatcherIO) {
            when (type) {
                villageId -> {
                    villageListResponse.postLoading()
                    villageListResponse.postValue(houseHoldRepository.getUserVillages(tag))
                }
            }
        }
    }

    fun registerHousehold(map: HashMap<String, Any>) {
        viewModelScope.launch(dispatcherIO) {
            try {
                houseHoldRegistrationLiveData.postLoading()
                householdEntityDetail = houseHoldRepository.createOrUpdateHouseHoldEntity(map)
                houseHoldRegistrationLiveData.postSuccess()
            } catch (e: Exception) {
                houseHoldRegistrationLiveData.postError()
            }
        }
    }

    fun updateHousehold(map: HashMap<String, Any>) {
        viewModelScope.launch(dispatcherIO) {
            try {
                houseHoldUpdateLiveData.postLoading()
                val householdEntity =
                    houseHoldRepository.createOrUpdateHouseHoldEntity(map, householdEntityDetail)
                houseHoldRepository.updateHouseHoldEntity(householdEntity)
                houseHoldUpdateLiveData.postSuccess()
            } catch (e: Exception) {
                houseHoldUpdateLiveData.postError()
            }
        }
    }

    fun getHouseholdDetailsByID(houseHoldId: Long) {
        try {
            viewModelScope.launch(dispatcherIO) {
                houseHoldDetailLiveData.postLoading()
                householdEntityDetail = houseHoldRepository.getHouseHoldDetailsById(houseHoldId)
                houseHoldDetailLiveData.postSuccess(householdEntityDetail)
            }
        } catch (e: Exception) {
            houseHoldDetailLiveData.postError()
        }
    }

    fun setCurrentLocation(location: Location) {
        this.lastLocation = location
    }

    fun getCurrentLocation(): Location? {
        return this.lastLocation
    }

}