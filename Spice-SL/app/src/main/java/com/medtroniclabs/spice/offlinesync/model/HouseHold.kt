package com.medtroniclabs.spice.offlinesync.model

import androidx.room.ColumnInfo
import androidx.room.Ignore

data class HouseHold(

    @ColumnInfo(name = "id")
    val referenceId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name= "villageName")
    val village: String,

    @ColumnInfo(name = "village_id")
    val villageId: Long,

    @ColumnInfo(name = "landmark")
    val landmark: String? = null,

    @ColumnInfo(name = "head_phone_number")
    val headPhoneNumber: String,

    @ColumnInfo(name = "no_of_people")
    val noOfPeople: Int,

    @ColumnInfo(name = "household_no")
    val householdNo: Int,

    @ColumnInfo(name = "is_owned_hand_washing_facility_with_soap")
    val ownedHandWashingFacilityWithSoap: Boolean,

    @ColumnInfo(name = "is_owned_a_treated_bed_net")
    val ownedTreatedBedNet: Boolean,

    @ColumnInfo(name = "is_owned_an_improved_latrine")
    val ownedAnImprovedLatrine: Boolean,

    @ColumnInfo(name = "bed_net_count")
    val bedNetCount: Int? = null,

    @ColumnInfo(name = "latitude")
    val latitude: Double = 77.2,

    @ColumnInfo(name = "longitude")
    val longitude: Double = 99.2,

    @ColumnInfo(name = "created_at")
    val createAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
) {
    @Ignore
    var provenance: ProvanceDto = ProvanceDto()
    @Ignore
    var householdMembers: MutableList<HouseHoldMember> = mutableListOf()
}
