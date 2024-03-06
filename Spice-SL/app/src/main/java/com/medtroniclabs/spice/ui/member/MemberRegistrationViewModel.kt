package com.medtroniclabs.spice.ui.member

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.HouseHoldRepository
import com.medtroniclabs.spice.repo.MemberRegistrationRepository
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
    var memberRegistrationLiveData = MutableLiveData<Resource<Long>>()
    var startAssessment: Boolean? = null
    val memberDetailsLiveData = MutableLiveData<Resource<HouseholdMemberEntity>>()
    val formLayoutsLiveData = MutableLiveData<Resource<String>>()

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

    fun registerHouseThenMember(
        householdEntity: HouseholdEntity,
        memberResultMap: HashMap<String, Any>,
    ) {
         memberRegistrationLiveData.postLoading()
          try {
              viewModelScope.launch(dispatcherIO) {
                  val houseHoldId = houseHoldRepository.insertHouseHoldEntity(householdEntity)
                  registerMember(memberResultMap, houseHoldId)
              }
          }catch (e: Exception) {
              memberRegistrationLiveData.postError(e.message)
          }
    }

    fun registerMember(map: HashMap<String, Any>, householdId: Long) {
         memberRegistrationLiveData.postLoading()
        try {
            viewModelScope.launch(dispatcherIO) {
                selectedHouseholdId = householdId
                val memberId = memberRegistrationRepository.registerMember(map, householdId, memberDetailsLiveData.value?.data)
                memberRegistrationLiveData.postSuccess(memberId)
            }
        } catch (e: Exception) {
            memberRegistrationLiveData.postError()
        }
    }


}
