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

    @ColumnInfo("age")
    var age: String = "",

    @ColumnInfo("gender")
    var gender: String = "",

    @ColumnInfo("household_head_relationship")
    var householdHeadRelationship: String = "",

    @ColumnInfo("household_id")
    var householdId: Long = 0,

    @ColumnInfo("patient_id")
    var patientId: String? = null,

    @ColumnInfo("fhir_id")
    var fhirId: String? = null,

    @ColumnInfo("is_synced")
    var isSynced: Boolean = false,

    @ColumnInfo("created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo("updated_at")
    var updatedAt: Long = System.currentTimeMillis()
)