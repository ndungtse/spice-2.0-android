package com.medtroniclabs.spice.ui.member

import android.content.Context
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams.HouseholdHead
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdMemberWithTb
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.entity.VillageEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.HouseholdHeadRelationship
import com.medtroniclabs.spice.mappingkey.MemberRegistration
import com.medtroniclabs.spice.model.medicalreview.AddMemberRegRequest
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.repo.HouseHoldRepository
import com.medtroniclabs.spice.repo.HouseholdMemberRepository
import com.medtroniclabs.spice.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemberRegistrationViewModel @Inject constructor(
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher,
    private val memberRegistrationRepository: HouseholdMemberRepository,
    private val houseHoldRepository: HouseHoldRepository,
    @ApplicationContext private val context: Context,
) : BaseViewModel(dispatcherIO) {
    var selectedHouseholdId: Long = -1L
    var memberRegistrationLiveData = MutableLiveData<Resource<Long>>()
    var startAssessment: Boolean? = null
    val memberDetailsLiveData = MutableLiveData<Resource<HouseholdMemberEntity>>()
    val formLayoutsLiveData = MutableLiveData<Resource<String>>()
    var medicalReviewFlow = false
    val addnewMemberReq = MutableLiveData<Resource<String>>()
    var villageDetails: List<VillageEntity>? = null
    var addNewMember: Boolean = false
    var memberDob: String? = null
    var isPhuWalkInsFlow: Boolean? = null
    val householdHeadDobLiveData = MutableLiveData<String?>()

    val householdMembersLiveData = MutableLiveData<List<HouseholdMemberWithTb>>()

    fun getHouseholdHeadDob(householdId: Long?) {
        if (householdId != null && householdId != -1L) {
            viewModelScope.launch(dispatcherIO) {
                householdHeadDobLiveData.postValue(memberRegistrationRepository.getHouseholdHeadDob(householdId))
            }
        } else {
            householdHeadDobLiveData.postValue(null)
        }
    }

    fun getFormData(formType: String) {
        viewModelScope.launch(dispatcherIO) {
//            if (formType == com.medtroniclabs.spice.common.DefinedParams.HOUSEHOLD_MEMBER_REGISTRATION) {
//                // Load from assets for member registration
//                try {
//                    val jsonString = CommonUtils.getStringFromAssets("member_registration.json", context.assets)
//                    formLayoutsLiveData.postValue(Resource(state = ResourceState.SUCCESS, data = jsonString))
//                } catch (e: Exception) {
//                    formLayoutsLiveData.postValue(Resource(state = ResourceState.ERROR))
//                }
//            } else {
            formLayoutsLiveData.postValue(houseHoldRepository.getFormData(formType))
//            }
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
        signature: String? = null,
    ) {
        memberRegistrationLiveData.postLoading()
        try {
            viewModelScope.launch(dispatcherIO) {
                location?.let {
                    householdEntity.latitude = it.latitude
                    householdEntity.longitude = it.longitude
                }

                // Update household name with member name if it's the household head
                val memberName = memberResultMap[MemberRegistration.name]
                if (memberName != null) {
                    householdEntity.name = CommonUtils.getStringOrEmptyString(memberName)
                    // Set isHouseholdHead to true when updating household name with member name
                    memberResultMap[MemberRegistration.isHouseholdHead] = true
                }

                val houseHoldId = houseHoldRepository.insertHouseHoldEntity(householdEntity)

                  /*
                   * Update Relation Household Head* */
                memberResultMap[HouseholdHeadRelationship] = HouseholdHead

                registerMember(
                    memberResultMap,
                    houseHoldId,
                    initial,
                    signature,
                    location,
                )
            }
        } catch (e: Exception) {
            memberRegistrationLiveData.postError(e.message)
        }
    }

    fun registerMember(
        map: HashMap<String, Any>,
        householdId: Long,
        initial: String? = null,
        signature: String? = null,
        location: Location?,
    ) {
        memberRegistrationLiveData.postLoading()
        try {
            viewModelScope.launch(dispatcherIO) {
                selectedHouseholdId = householdId
                memberDob = if (map.containsKey(MemberRegistration.dateOfBirth)) {
                    CommonUtils.getStringOrEmptyString(map[MemberRegistration.dateOfBirth])
                } else {
                    null
                }
                val memberId = memberRegistrationRepository.registerMember(
                    map,
                    householdId,
                    memberDetailsLiveData.value?.data,
                    initial = initial,
                    signature = signature,
                    isPhuWalkInFlow = isPhuWalkInsFlow,
                    location = location,
                )
                memberRegistrationRepository.updateHeadPhoneNumber(householdId, map)
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

    fun addNewMember(
        map: HashMap<String, Any>?,
        formGenerator: FormGenerator,
        location: Location?,
    ) {
        if (map == null) return
        val villageId = map[MemberRegistration.villageId]?.toString()?.toIntOrNull()
        val addMemberRegRequest = AddMemberRegRequest().apply {
            name = map[MemberRegistration.name]?.toString().orEmpty()
            this.villageId = villageId?.toString().orEmpty()
            village = villageId
                ?.let { id ->
                    villageDetails?.find { it.id == id.toLong() }?.name.orEmpty()
                }.orEmpty()
            dateOfBirth = map[MemberRegistration.dateOfBirth]?.toString().orEmpty()
            gender = map[MemberRegistration.gender]?.toString().orEmpty()
            phoneNumber = map[MemberRegistration.phoneNumber]?.toString().orEmpty()
            phoneNumberCategory = null
            provenance = ProvanceDto()
            isPregnant = null
            location?.let {
                longitude = location.longitude
                latitude = location.latitude
            }
        }
        viewModelScope.launch(dispatcherIO) {
            addnewMemberReq.postLoading()
            addnewMemberReq.postValue(houseHoldRepository.addNewMember(addMemberRegRequest))
        }
    }

    fun getHouseholdMembers(householdId: Long) =
        viewModelScope.launch(dispatcherIO) {
            householdMembersLiveData.postValue(houseHoldRepository.getAllHouseHoldMembersLiveData(householdId).value)
        }
}
