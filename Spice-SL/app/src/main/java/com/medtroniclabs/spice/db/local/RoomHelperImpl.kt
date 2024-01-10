package com.medtroniclabs.spice.db.local

import com.medtroniclabs.spice.db.dao.HouseholdDAO
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import javax.inject.Inject

class RoomHelperImpl @Inject constructor(
    private val householdDAO: HouseholdDAO,
) : RoomHelper {
    override suspend fun saveFormEntry(householdEntity: HouseholdEntity): Long {
        return householdDAO.insertHouseHold(householdEntity)
    }
}