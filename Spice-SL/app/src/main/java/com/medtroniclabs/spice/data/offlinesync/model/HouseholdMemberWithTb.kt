package com.medtroniclabs.spice.data.offlinesync.model

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class HouseholdMemberWithTb(
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
    var householdId: Long? = null,

    @ColumnInfo("villageId")
    var villageId: Long? = null,

    @ColumnInfo("patient_id")
    var patientId: String? = null,

    var isPregnant: Boolean? = null,

    var parentId: String? = null,

    var isActive: Boolean = true,

    var signature: String? = null,

    var initial: String? = null,

    val version: String? = null,

    val lastUpdated: String? = null,

    var localSignatureFile: String? = null,

    var motherReferenceId: Long? = null,

    var deceasedReason: String? = null,

    var latitude: Double = 0.0,

    var longitude: Double = 0.0,

    val tBContactTraceStatus:Int? = null,

    @ColumnInfo("fhir_id")
    val fhirId: String? = null,

    val diagnoses: String? = null

)
