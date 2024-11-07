package com.medtroniclabs.spice.ui.phuwalkins.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.data.offlinesync.model.UnAssignedHouseholdMemberDetail
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.di.IoDispatcher
import com.medtroniclabs.spice.repo.HouseHoldRepository
import com.medtroniclabs.spice.repo.HouseholdMemberRepository
import com.medtroniclabs.spice.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhuWalkInsViewModel @Inject constructor(
    private val householdMemberRepository: HouseholdMemberRepository,
    private val houseHoldRepository: HouseHoldRepository,
    @IoDispatcher override var dispatcherIO: CoroutineDispatcher
) : BaseViewModel(dispatcherIO) {

    var memberID:Long=-1L

    var fhirMemberID:Long=-1L

    val unAssignedMembers: LiveData<List<UnAssignedHouseholdMemberDetail>> =
        householdMemberRepository.getUnAssignedHouseholdMember()

    fun getPatientName(
        context: Context,
        name: String?,
        dob: String?,
        gender: String?
    ): String {
        return context.getString(
            R.string.household_summary_member_info,
            name,
            CommonUtils.getAgeFromDOB(
                dob,
                context
            ),
            CommonUtils.getGenderText(gender, context)
        )
    }


    fun getFilteredHouseholdsLiveData(villageIds: Long): LiveData<List<HouseHoldEntityWithMemberCount>> {
        return houseHoldRepository.getFilteredHouseholdsLiveData(
            "",
            villageIds = listOf(villageIds),
            ""
        )
    }

    fun getSearchHouseholdsLiveData(search: String, villageId: Long): LiveData<List<HouseHoldEntityWithMemberCount>> {
      val listVillage= ArrayList<Long>()
        listVillage.add(villageId)
        return houseHoldRepository.getFilteredHouseholdsLiveData(
            search,
            villageIds = listVillage,
            ""
        )
    }

    fun saveCallHistory() {
        viewModelScope.launch(dispatcherIO) {
            SecuredPreference.getString(DefinedParams.houseHoldLinkStartTiming)?.let {
                householdMemberRepository.addLinkMemberCall(
                    memberID.toString(),
                    callStartTime = it,
                    callEndTime =  com.medtroniclabs.spice.app.analytics.utils.CommonUtils.getCurrentDateTimeInLocalTime()
                )
            }
        }
    }

}