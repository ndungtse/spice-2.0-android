package com.medtroniclabs.spice.db.local

import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount

interface RoomHelper {
    suspend fun saveHouseHoldEntry(householdEntity: HouseholdEntity): Long
    suspend fun getHouseHoldList(): ArrayList<HouseHoldEntityWithMemberCount>
    suspend fun getLastHouseholdNo(villageId: Long): Long?
    suspend fun searchByHouseholdNameOrNo(searchTerm: String): ArrayList<HouseHoldEntityWithMemberCount>
    suspend fun getHouseHoldDetailsById(houseHoldId: Long): HouseholdEntity
    suspend fun registerMember(householdMemberEntity: HouseholdMemberEntity):Long
    suspend fun getAllHouseHoldMemberList(houseHoldId: Long): ArrayList<HouseholdMemberEntity>

}