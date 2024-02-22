package com.medtroniclabs.spice.ui.household.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.CommonUtils.getIntegerOrNull
import com.medtroniclabs.spice.common.CommonUtils.getIsBooleanFromString
import com.medtroniclabs.spice.common.CommonUtils.getLongOrNull
import com.medtroniclabs.spice.common.CommonUtils.getStringOrEmptyString
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.HouseHoldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HouseRegistrationViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val houseHoldRepository: HouseHoldRepository
) : ViewModel() {

    var houseHoldRegistrationLiveData = MutableLiveData<Resource<Long>>()

    var isMemberRegistration: Boolean = false

    var householdEntityDetail: HouseholdEntity? = null

    val formLayoutsLiveData = MutableLiveData<Resource<String>>()

    fun registerHousehold(map: HashMap<String, Any>) {
        viewModelScope.launch(dispatcherIO) {
            try {
                houseHoldRegistrationLiveData.postLoading()
                val householdName = map[HouseHoldRegistration.householdName]
                val headPhoneNumber = map[HouseHoldRegistration.headPhoneNumber]
                val landmark = map[HouseHoldRegistration.landmark]
                val villageID = map[HouseHoldRegistration.villageId]
                val noOfPeople = map[HouseHoldRegistration.noOfPeople]
                val isOwnedAnImprovedLatrine = map[HouseHoldRegistration.isOwnedAnImprovedLatrine]
                val isOwnedHandWashingFacilityWithSoap =
                    map[HouseHoldRegistration.isOwnedHandWashingFacilityWithSoap]
                val isOwnedATreatedBedNet = map[HouseHoldRegistration.isOwnedATreatedBedNet]
                val bedNetCount = map[HouseHoldRegistration.bedNetCount]
                val villageLongID = getLongOrNull(villageID) ?: 0
                val lastHouseHoldNo = houseHoldRepository.getLastHouseholdNo(villageLongID) ?: 0
                val householdEntity = HouseholdEntity(
                    id = 0,
                    householdNo = lastHouseHoldNo + 1,
                    name = getStringOrEmptyString(householdName),
                    villageId = villageLongID,
                    landmark = getStringOrEmptyString(landmark),
                    headPhoneNumber = getStringOrEmptyString(headPhoneNumber),
                    noOfPeople = getIntegerOrNull(noOfPeople) ?: 0,
                    isOwnedAnImprovedLatrine = getIsBooleanFromString(isOwnedAnImprovedLatrine),
                    isOwnedHandWashingFacilityWithSoap = getIsBooleanFromString(
                        isOwnedHandWashingFacilityWithSoap
                    ),
                    isOwnedATreatedBedNet = getIsBooleanFromString(isOwnedATreatedBedNet),
                    bedNetCount = getIntegerOrNull(bedNetCount)
                )

                householdEntityDetail = householdEntity
                houseHoldRegistrationLiveData.postSuccess()
            } catch (e: Exception) {
                houseHoldRegistrationLiveData.postError()
            }
        }
    }


    fun getFormData(formType: String) {
        viewModelScope.launch(dispatcherIO) {
            houseHoldRepository.getFormData(formType, formLayoutsLiveData)
        }
    }
}