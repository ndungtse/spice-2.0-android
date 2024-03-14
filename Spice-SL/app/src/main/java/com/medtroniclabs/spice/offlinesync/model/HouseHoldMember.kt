package com.medtroniclabs.spice.offlinesync.model

import androidx.room.ColumnInfo
import androidx.room.Ignore

data class HouseHoldMember(

    @ColumnInfo("id")
    val referenceId: String,

    @ColumnInfo("name")
    val name: String,

    @ColumnInfo("household_id")
    var householdId: Long = 0,

    @ColumnInfo("date_of_birth")
    val dateOfBirth: String,

    @ColumnInfo("phone_number")
    val phoneNumber: String,

    @ColumnInfo("phone_number_category")
    val phoneNumberCategory: String,

    @ColumnInfo("patient_id")
    val patientId: String,

    @ColumnInfo("gender")
    val gender: String,

    @ColumnInfo("household_head_relationship")
    val householdRelationship: String,

    @ColumnInfo("created_at")
    val createdAt: Long,

    @ColumnInfo("updated_at")
    val updatedAt: Long
) {
    @Ignore
    var provenance: ProvanceDto = ProvanceDto()
}
