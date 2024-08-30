package com.medtroniclabs.spice.ui.member

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.app.analytics.db.AnalyticsRepository
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils
import com.medtroniclabs.spice.appextensions.isVisible
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.isOwnedATreatedBedNet
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration.isPregnant
import com.medtroniclabs.spice.mappingkey.MemberRegistration
import com.medtroniclabs.spice.model.medicalreview.AddMemberRegRequest
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.HouseHoldRepository
import com.medtroniclabs.spice.repo.HouseholdMemberRepository
import com.medtroniclabs.spice.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemberRegistrationViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val memberRegistrationRepository: HouseholdMemberRepository,
    private val houseHoldRepository: HouseHoldRepository,
) : BaseViewModel(dispatcherIO) {

    var selectedHouseholdId: Long = -1L
    var memberRegistrationLiveData = MutableLiveData<Resource<Long>>()
    var startAssessment: Boolean? = null
    val memberDetailsLiveData = MutableLiveData<Resource<HouseholdMemberEntity>>()
    val formLayoutsLiveData = MutableLiveData<Resource<String>>()
    var medicalReviewFlow=false
    val addnewMemberReq=MutableLiveData<Resource<String>>()
    var villageDetails:List<VillageEntity>?= null
    var addNewMember: Boolean = false

    fun getFormData(formType: String) {
        viewModelScope.launch(dispatcherIO) {
            formLayoutsLiveData.postValue(houseHoldRepository.getFormData(formType))
        }
    }

    fun getMemberDetailsByID(memberId: Long) {
        if (memberId == -1L) {
            return
        }
        viewModelScope.launch(dispatcherIO) {
            memberDetailsLiveData.postLoading()
            memberDetailsLiveData.postValue(memberRegistrationRepository.getMemberDetailsByID(memberId))
        }
    }

    fun registerHouseThenMember(
        householdEntity: HouseholdEntity,
        memberResultMap: HashMap<String, Any>,
        location: Location?,
        initial: String? = null,
        signature: String? = null
    ) {
         memberRegistrationLiveData.postLoading()
          try {
              viewModelScope.launch(dispatcherIO) {
                  location?.let {
                      householdEntity.latitude = it.latitude
                      householdEntity.longitude = it.longitude
                  }
                  val houseHoldId = houseHoldRepository.insertHouseHoldEntity(householdEntity)
                  registerMember(memberResultMap, houseHoldId, initial, signature)
              }
          }catch (e: Exception) {
              memberRegistrationLiveData.postError(e.message)
          }
    }

    fun registerMember(map: HashMap<String, Any>, householdId: Long, initial: String? = null, signature: String? = null) {
         memberRegistrationLiveData.postLoading()
        try {
            viewModelScope.launch(dispatcherIO) {
                selectedHouseholdId = householdId
                val memberId = memberRegistrationRepository.registerMember(
                    map,
                    householdId,
                    memberDetailsLiveData.value?.data,
                    initial = initial,
                    signature = signature
                )
                if (memberId == null) {
                    memberRegistrationLiveData.postError()
                } else {
                    memberRegistrationLiveData.postSuccess(memberId)
                }
            }
        } catch (e: Exception) {
            memberRegistrationLiveData.postError()
        }
    }

    fun addNewMember(map: HashMap<String, Any>?, formGenerator: FormGenerator) {
        if (map == null) return
        val villageId = map[MemberRegistration.villageId]?.toString()?.toIntOrNull()
        val addMemberRegRequest = AddMemberRegRequest().apply {
            name = map[MemberRegistration.name]?.toString().orEmpty()
            this.villageId = villageId?.toString().orEmpty()
            village = villageId?.let { id ->
                villageDetails?.find { it.id == id.toLong() }?.name.orEmpty()
            }.orEmpty()
            dateOfBirth = map[MemberRegistration.dateOfBirth]?.toString().orEmpty()
            gender = map[MemberRegistration.gender]?.toString().orEmpty()
            phoneNumber = map[MemberRegistration.phoneNumber]?.toString().orEmpty()
            phoneNumberCategory = map[MemberRegistration.phoneNumberCategory]?.toString().orEmpty()
            provenance = ProvanceDto()
            isPregnant = map[MemberRegistration.isPregnant]?.let { CommonUtils.getIsBooleanFromString(it) }
        }
        viewModelScope.launch(dispatcherIO) {
            addnewMemberReq.postLoading()
            addnewMemberReq.postValue(houseHoldRepository.addNewMember(addMemberRegRequest))
        }
    }

}
