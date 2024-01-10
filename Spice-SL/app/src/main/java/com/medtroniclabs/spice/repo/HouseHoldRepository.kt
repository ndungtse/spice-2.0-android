package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.network.ApiHelper
import javax.inject.Inject

class HouseHoldRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {
    suspend fun registerHousehold(householdEntity: HouseholdEntity): Long =
        roomHelper.saveFormEntry(householdEntity)

}