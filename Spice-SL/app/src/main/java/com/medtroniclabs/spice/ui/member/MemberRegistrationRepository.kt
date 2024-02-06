package com.medtroniclabs.spice.ui.member

import androidx.lifecycle.MutableLiveData
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.postLoading
import com.medtroniclabs.spice.appextensions.postSuccess
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import com.medtroniclabs.spice.network.resource.Resource
import javax.inject.Inject

class MemberRegistrationRepository @Inject constructor(
    private var roomHelper: RoomHelper
) {
    suspend fun registerMember(memberEntity: HouseholdMemberEntity): Long =
        roomHelper.registerMember(memberEntity)

    suspend fun getMemberDetailsByID(
        memberId: Long,
        memberDetailsLiveData: MutableLiveData<Resource<HouseholdMemberEntity>>
    ) {
        try {
            memberDetailsLiveData.postLoading()
            val memberEntity = roomHelper.getMemberDetailsByID(memberId)
            memberDetailsLiveData.postSuccess(memberEntity)
        } catch (e: Exception) {
            memberDetailsLiveData.postError()
        }
    }

}