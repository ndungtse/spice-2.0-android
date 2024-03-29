package com.medtroniclabs.spice.offlinesync.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.offlinesync.utils.OfflineSyncStatus

data class HouseHoldMember(

    @ColumnInfo("id")
    val referenceId: String?,

    @ColumnInfo(name = "fhir_id")
    var id: String? = null,

    @ColumnInfo("name")
    val name: String,

    @ColumnInfo("household_id")
    var householdReferenceId: String? = null,

    @ColumnInfo("household_fhir_id")
    var householdId: String? = null,

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
    val householdRelationship: String?,

    @ColumnInfo("created_at")
    val createdAt: Long,

    @ColumnInfo("updated_at")
    val updatedAt: Long
) {
    @Ignore
    var provenance: ProvanceDto = ProvanceDto()

    fun toHouseholdMemberEntity(hhId: Long, status: OfflineSyncStatus, id: Long = 0): HouseholdMemberEntity {
        return HouseholdMemberEntity(
            id = id,
            name = this.name,
            phoneNumber = this.phoneNumber,
            phoneNumberCategory = "", // Need to check with backend
            dateOfBirth = this.dateOfBirth,
            gender = this.gender,
            householdHeadRelationship = this.householdRelationship ?: "",
            householdId = hhId,
            patientId = this.patientId
        ).apply {
            fhirId = this@HouseHoldMember.id.toString()
            sync_status = status
        }
    }
}
