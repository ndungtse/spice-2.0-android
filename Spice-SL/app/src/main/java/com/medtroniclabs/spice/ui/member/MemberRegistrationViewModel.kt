package com.medtroniclabs.spice.ui.member

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.HouseHoldRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemberRegistrationViewModel @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
    private val memberRegistrationRepository: MemberRegistrationRepository,
    private val houseHoldRepository: HouseHoldRepository
) : ViewModel() {

    var selectedHouseholdId: Long = -1L
    private var noOfPerson: Int = 0
    var memberRegistrationLiveData = MutableLiveData<Resource<Long>>()
    var startAssessment: Boolean? = null
    val memberDetailsLiveData = MutableLiveData<Resource<HouseholdMemberEntity>>()
    val formLayoutsLiveData = MutableLiveData<Resource<String>>()

    fun registerMember(map: HashMap<String, Any>, householdId: Long) {
        if (householdId == -1L)
            return
        try {
            viewModelScope.launch(dispatcherIO) {
                selectedHouseholdId = householdId
                memberRegistrationRepository.registerMember(
                    map, householdId, memberRegistrationLiveData, memberDetailsLiveData, noOfPerson
                )
            }
        } catch (e: Exception) {
            memberRegistrationLiveData.postError()
        }
    }

    fun registerHouseThenMember(
        householdEntity: HouseholdEntity,
        memberResultMap: HashMap<String, Any>,
    ) {
        viewModelScope.launch(dispatcherIO) {
            val count =  householdEntity.noOfPeople
            val rowId = houseHoldRepository.registerHousehold(householdEntity)
            noOfPerson = count
            registerMember(memberResultMap, rowId)
        }
    }

    fun getFormData(formType: String) {
        viewModelScope.launch(dispatcherIO) {
            houseHoldRepository.getFormData(formType, formLayoutsLiveData)
        }
    }

    fun getMemberDetailsByID(memberId: Long) {
        if (memberId == -1L) {
            return
        }
        viewModelScope.launch(dispatcherIO) {
            memberRegistrationRepository.getMemberDetailsByID(memberId, memberDetailsLiveData)
        }
    }
}
