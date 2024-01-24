package com.medtroniclabs.spice.repo

import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.db.local.RoomHelper
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.network.ApiHelper
import javax.inject.Inject

class HouseHoldRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    private var roomHelper: RoomHelper
) {
    suspend fun registerHousehold(householdEntity: HouseholdEntity): Long =
        roomHelper.saveHouseHoldEntry(householdEntity)
    suspend fun getHouseHoldList():ArrayList<HouseHoldEntityWithMemberCount>  = roomHelper.getHouseHoldList()
    suspend fun getLastHouseholdNo(villageId: Long): Long? = roomHelper.getLastHouseholdNo(villageId)
    suspend fun searchByHouseholdNameOrNo(searchTerm: String) = roomHelper.searchByHouseholdNameOrNo(searchTerm)
    suspend fun getHouseHoldDetailsById(houseHoldId:Long) = roomHelper.getHouseHoldDetailsById(houseHoldId)
    suspend fun getAllHouseHoldMemberList(houseHoldId: Long): ArrayList<HouseholdMemberEntity> = roomHelper.getAllHouseHoldMemberList(houseHoldId)

}