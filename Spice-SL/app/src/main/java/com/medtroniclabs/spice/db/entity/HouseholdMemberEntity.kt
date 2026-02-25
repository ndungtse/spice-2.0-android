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
    var phoneNumber: String? = "",
    @ColumnInfo("date_of_birth")
    var dateOfBirth: String = "",
    @ColumnInfo("gender")
    var gender: String = "",
    @ColumnInfo("household_id")
    var householdId: Long? = null,
    @ColumnInfo("villageId")
    var villageId: Long? = null,
    @ColumnInfo("patient_id")
    var patientId: String? = null,
    var isActive: Boolean = true,
    val version: String? = null,
    val lastUpdated: String? = null,
    var motherReferenceId: Long? = null,
    var deceasedReason: String? = null,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    @ColumnInfo("id_type")
    var idType: String = "",
    @ColumnInfo("national_id")
    var nationalId: String? = null,
    @ColumnInfo("is_house_hold_head")
    var isHouseholdHead: Boolean = false,
    @ColumnInfo("household_fhir_id")
    var householdFhirId: String? = null,
) : BaseEntity()
