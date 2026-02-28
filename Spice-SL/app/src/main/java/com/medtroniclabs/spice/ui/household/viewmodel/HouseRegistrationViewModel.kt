package com.medtroniclabs.spice.ui.household.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.LocalSpinnerResponse
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineConstant
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.villageId
import com.medtroniclabs.spice.mappingkey.MemberRegistration.ID_GUARDIAN
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.HouseHoldRepository
import com.medtroniclabs.spice.repo.HouseholdMemberRepository
import com.medtroniclabs.spice.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HouseRegistrationViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val houseHoldRepository: HouseHoldRepository,
    private val houseHoldRepositoryMember: HouseholdMemberRepository,
) : BaseViewModel(dispatcherIO) {
    var houseHoldRegistrationLiveData = MutableLiveData<Resource<Long>>()
    var isMemberRegistration: Boolean = false
    var householdEntityDetail: HouseholdEntity? = null
    var isCreateHouseholdForPhu: Boolean = false
    var isPhuWalkInsFlow: Boolean = false
    val formLayoutsLiveData = MutableLiveData<Resource<String>>()
    var houseHoldUpdateLiveData = MutableLiveData<Resource<Long>>()
    val houseHoldDetailLiveData = MutableLiveData<Resource<HouseholdEntity>>()
    var householdId: Long = -1L
    var villageListResponse = MutableLiveData<Resource<LocalSpinnerResponse>>()
    var memberVillageListResponse = MutableLiveData<Resource<LocalSpinnerResponse>>()
    var shasthyaShebikaListResponse = MutableLiveData<Resource<LocalSpinnerResponse>>()
    var subVillageListResponse = MutableLiveData<Resource<LocalSpinnerResponse>>()
    var guardianMembers = MutableLiveData<Resource<LocalSpinnerResponse>>()
    var memberID: Long = -1L
    private var lastLocation: Location? = null
    var eventName: String = ""

    /**
     * Selected sub village
     */
    var selectedSubVillageId: Long = -1

    var signatureFilename: String? = null
    var initialValue: String? = null
    val generatedHouseholdNumberLiveData = MutableLiveData<String>()

    /**
     * Generates household number in the format HH<current count of households based on subvillage + 1>
     */
    fun generateHouseholdNumber() {
        viewModelScope.launch(dispatcherIO) {
            val currentHouseholdsCount = houseHoldRepository.getHouseholdsCountBasedSubVillage(selectedSubVillageId)
            val householdNumber = "HH${currentHouseholdsCount + 1}"
            generatedHouseholdNumberLiveData.postValue(householdNumber)
        }
    }

    fun getFormData(formType: String) {
        viewModelScope.launch(dispatcherIO) {
            formLayoutsLiveData.postLoading()
            formLayoutsLiveData.postValue(houseHoldRepository.getFormData(formType))
        }
    }

    fun loadDataCacheByType(
        type: String,
        tag: String,
    ) {
        viewModelScope.launch(dispatcherIO) {
            when (type) {
                villageId -> {
                    villageListResponse.postLoading()
                    villageListResponse.postValue(houseHoldRepository.getUserVillages(tag))
                }
            }
        }
    }

    fun loadVillageDataCacheByType(
        type: String,
        tag: String,
    ) {
        viewModelScope.launch(dispatcherIO) {
            when (type) {
                villageId -> {
                    memberVillageListResponse.postLoading()
                    memberVillageListResponse.postValue(houseHoldRepository.getUserLinkedVillages(tag))
                }

                ID_GUARDIAN -> {
                    guardianMembers.postLoading()
                    guardianMembers.postValue(houseHoldRepository.getGuardianMembers(tag, householdId, memberID))
                }
            }
        }
    }

    fun loadShasthyaShebikaDataCacheByType(
        type: String,
        tag: String,
    ) {
        viewModelScope.launch(dispatcherIO) {
            val userId = SecuredPreference.getUserId()
            shasthyaShebikaListResponse.postLoading()
            shasthyaShebikaListResponse.postValue(houseHoldRepository.getShasthyaShebikasByKormiId(userId))
        }
    }

    fun loadSubVillageDataCacheByType(
        type: String,
        tag: String,
        shasthyaShebikaId: Long,
    ) {
        viewModelScope.launch(dispatcherIO) {
            subVillageListResponse.postLoading()
            subVillageListResponse.postValue(houseHoldRepository.getSubVillagesByShasthyaShebikaId(shasthyaShebikaId))
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

    fun getCurrentLocation(): Location? = this.lastLocation

    fun updateMemberAsAssigned(
        memberID: Long?,
        hhmId: Long? = null,
        hhId: Long? = null,
    ) {
        viewModelScope.launch(dispatcherIO) {
            if (hhmId != null && hhId != null) {
                val tbPatientIds = houseHoldRepositoryMember.getTbPatientLocalIdByHouseholdId(hhId)
                val isTbPatient = houseHoldRepositoryMember.isTbPatient(memberID.toString())

                when {
                    tbPatientIds.isNotEmpty() && !isTbPatient -> {
                        // Household has TB patients but this member is not one — update contact tracing status
                        houseHoldRepositoryMember.updateContactTracingStatus(
                            hhmId,
                            OfflineConstant.CONTACT_TRACING_YET_TO_TAKE,
                        )
                    }

                    tbPatientIds.isEmpty() && isTbPatient -> {
                        // No TB patients in household but this member is one — update all household contact tracing statuses
                        houseHoldRepositoryMember.updateContactTracingForLinkTbPatient(hhmId, hhId)
                    }

                    else -> {
                        houseHoldRepositoryMember.updateContactTracingStatus(hhmId, null)
                    }
                }
            }

            houseHoldRepositoryMember.updateMemberAsAssigned(memberID.toString())
        }
    }
}
