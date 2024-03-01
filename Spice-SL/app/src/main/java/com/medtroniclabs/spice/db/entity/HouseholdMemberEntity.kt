package com.medtroniclabs.spice.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "household_member")
data class HouseholdMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Long,
    val name : String,
    @ColumnInfo("phone_number")
    val phoneNumber:String,
    @ColumnInfo("phone_number_category")
    val phoneNumberCategory:String,
    @ColumnInfo("date_of_birth")
    val dateOfBirth:String,
    val age:String,
    val gender: String,
    @ColumnInfo("household_head_relationship")
    val householdHeadRelationship:String,
    @ColumnInfo("household_id")
    val householdId:Long,
    @ColumnInfo("patient_id")
    val patientId: String? = null,
    @ColumnInfo("created_at")
    val createdAt: Long,
    @ColumnInfo("updated_at")
    var updatedAt: Long
)