package com.medtroniclabs.spice.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "HouseHoldMember")
data class HouseholdMemberEntity(

    @PrimaryKey(autoGenerate = true)
    val id : Long = 0,

    @ColumnInfo("name")
    var name : String = "",

    @ColumnInfo("phone_number")
    var phoneNumber:String = "",

    @ColumnInfo("phone_number_category")
    var phoneNumberCategory:String = "",

    @ColumnInfo("date_of_birth")
    var dateOfBirth:String = "",

    @ColumnInfo("age")
    var age:String = "",

    @ColumnInfo("gender")
    var gender: String = "",

    @ColumnInfo("household_head_relationship")
    var householdHeadRelationship:String = "",

    @ColumnInfo("household_id")
    var householdId:Long = 0,

    @ColumnInfo("patient_id")
    var patientId: String? = null,

    @ColumnInfo("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo("updated_at")
    var updatedAt: Long  = System.currentTimeMillis()
)