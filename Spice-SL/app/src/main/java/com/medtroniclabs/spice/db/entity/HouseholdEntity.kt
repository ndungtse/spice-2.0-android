package com.medtroniclabs.spice.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.medtroniclabs.spice.db.entity.EntitiesName.HOUSEHOLD

@Entity(tableName = HOUSEHOLD)
data class HouseholdEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "household_no")
    var householdNo: Long? = null,

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "village_id")
    var villageId: Long = 0,

    @ColumnInfo(name = "no_of_people")
    var noOfPeople: Int = 0,

    @ColumnInfo(name = "shasthya_shebika_id")
    var shasthyaShebikaId: Long? = null,

    @ColumnInfo(name = "sub_village_id")
    var subVillageId: Long? = null,

    @ColumnInfo(name = "household_type")
    var householdType: String? = null,

    @ColumnInfo(name = "monthly_income")
    var monthlyIncome: Double? = null,

    @ColumnInfo("latitude")
    var latitude: Double = 0.0,

    @ColumnInfo("longitude")
    var longitude: Double = 0.0,

    val version: String? = null,

    val lastUpdated: String? = null,
): BaseEntity()

