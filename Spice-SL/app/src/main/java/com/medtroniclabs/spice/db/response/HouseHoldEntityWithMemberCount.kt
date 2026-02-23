package com.medtroniclabs.spice.db.response

import androidx.room.ColumnInfo

data class HouseHoldEntityWithMemberCount(
    val id: Long,
    @ColumnInfo("household_no")
    val householdNo: Long? = null,
    val name: String,
    @ColumnInfo("village_id")
    val villageId: Long,
    @ColumnInfo("no_of_people")
    val noOfPeople: Int,
    @ColumnInfo("shasthya_shebika_id")
    val shasthyaShebikaId: Long? = null,
    @ColumnInfo("sub_village_id")
    val subVillageId: Long? = null,
    @ColumnInfo("household_type")
    val householdType: String? = null,
    @ColumnInfo("monthly_income")
    val monthlyIncome: Double? = null,
    @ColumnInfo("member_count")
    val registerMemberCount: Int = 0,
    @ColumnInfo("village_name")
    val villageName: String,
)
