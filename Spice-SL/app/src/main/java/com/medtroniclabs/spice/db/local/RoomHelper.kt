package com.medtroniclabs.spice.db.local

import com.medtroniclabs.spice.db.entity.HouseholdEntity

interface RoomHelper {
    suspend fun saveFormEntry(householdEntity: HouseholdEntity):Long
}