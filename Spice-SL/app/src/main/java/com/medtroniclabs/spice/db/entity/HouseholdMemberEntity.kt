package com.medtroniclabs.spice.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.medtroniclabs.spice.db.entity.EntitiesName.HOUSEHOLD_MEMBER

@Entity(tableName = HOUSEHOLD_MEMBER)
data class HouseholdMemberEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo("name")
    var name: String = "",

    @ColumnInfo("phone_number")
    var phoneNumber: String = "",

    @ColumnInfo("phone_number_category")
    var phoneNumberCategory: String = "",

    @ColumnInfo("date_of_birth")
    var dateOfBirth: String = "",

    @ColumnInfo("gender")
    var gender: String = "",

    @ColumnInfo("household_head_relationship")
    var householdHeadRelationship: String = "",

    @ColumnInfo("household_id")
    var householdId: Long = 0,

    @ColumnInfo("patient_id")
    var patientId: String? = null,

    var isPregnant : Boolean? = null
) : BaseEntity()