package com.medtroniclabs.spice.ui.member

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.common.CommonUtils.getStringOrEmptyString
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Month
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Week
import com.medtroniclabs.spice.formgeneration.config.DefinedParams.Year
import com.medtroniclabs.spice.mappingkey.MemberRegistration
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

    var householdId: Long = -1L

    private var noOfPerson: Int = 0

    var memberRegistrationLiveData = MutableLiveData<Resource<Long>>()

    var startAssessment: Boolean? = null

    fun registerMember(map: HashMap<String, Any>) {
        if (householdId == -1L)
            return
        try {
            viewModelScope.launch(dispatcherIO) {
                memberRegistrationLiveData.postLoading()
                val name = map[MemberRegistration.name]
                val phoneNumber = map[MemberRegistration.phoneNumber]
                val phoneNumberCategory = map[MemberRegistration.phoneNumberCategory]
                val dateOfBirth = map[MemberRegistration.dateOfBirth]
                val age = calculateAgeString(map)
                val nationalId = map[MemberRegistration.nationalId]
                val gender = map[MemberRegistration.gender]
                val householdHeadRelationship = map[MemberRegistration.householdHeadRelationship]

                val memberRegistrationEntity = HouseholdMemberEntity(
                    id = 0,
                    name = getStringOrEmptyString(name),
                    phoneNumber = getStringOrEmptyString(phoneNumber),
                    phoneNumberCategory = getStringOrEmptyString(phoneNumberCategory),
                    dateOfBirth = getStringOrEmptyString(dateOfBirth),
                    age = age,
                    nationalId = getStringOrEmptyString(nationalId),
                    gender = getStringOrEmptyString(gender),
                    householdHeadRelationship = getStringOrEmptyString(householdHeadRelationship),
                    householdId = householdId
                )

                val rowId = memberRegistrationRepository.registerMember(memberRegistrationEntity)
                val getCountOfHouseHold = memberRegistrationRepository.getMemberCountPerHouseHold(householdId)
                if (getCountOfHouseHold > noOfPerson) {
                    memberRegistrationRepository.updateHeadCount(householdId, getCountOfHouseHold)
                }
                memberRegistrationLiveData.postSuccess(rowId)
            }
        } catch (e: Exception) {
            memberRegistrationLiveData.postError()
        }
    }

    private fun calculateAgeString(map: HashMap<String, Any>): String {
        val year = map[Year]
        val month = map[Month]
        val week = map[Week]
        return "${year?.toString() ?: ""}/${month?.toString() ?: ""}/${week?.toString() ?: ""}"
    }

    fun registerHouseThenMember(
        householdEntity: HouseholdEntity,
        memberResultMap: HashMap<String, Any>,
    ) {
        viewModelScope.launch(dispatcherIO) {
            val count =  householdEntity.noOfPeople
            val rowId = houseHoldRepository.registerHousehold(householdEntity)
            householdId = rowId
            noOfPerson = count
            registerMember(memberResultMap)
        }
    }
}
