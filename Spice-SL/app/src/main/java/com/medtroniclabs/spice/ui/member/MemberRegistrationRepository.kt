package com.medtroniclabs.spice.ui.member

import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class MemberRegistrationRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {
    suspend fun registerMember(memberEntity: HouseholdMemberEntity): Long =
        roomHelper.registerMember(memberEntity)

}