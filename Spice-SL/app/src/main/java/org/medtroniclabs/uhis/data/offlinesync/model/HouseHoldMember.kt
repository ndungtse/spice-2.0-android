package org.medtroniclabs.uhis.data.offlinesync.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import org.medtroniclabs.uhis.appextensions.convertToUtcDateTime
import org.medtroniclabs.uhis.data.offlinesync.utils.OfflineSyncStatus
import org.medtroniclabs.uhis.db.entity.HouseholdMemberEntity

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
    @ColumnInfo("patient_id")
    val patientId: String? = null,
    @ColumnInfo("gender")
    val gender: String,
    @ColumnInfo("created_at")
    val createdAt: Long,
    @ColumnInfo("updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "village_name")
    val village: String,
    @ColumnInfo(name = "village_id")
    val villageId: Long,
    @ColumnInfo(name = "sub_village_name")
    val subVillage: String? = null,
    @ColumnInfo(name = "sub_village_id")
    val subVillageId: Long? = null,
    val motherReferenceId: String? = null,
    val isActive: Boolean = true,
    val version: String? = null,
    val lastUpdated: String? = null,
    var deceasedReason: String? = null,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    @ColumnInfo("id_type")
    val idType: String? = null,
    @ColumnInfo("national_id")
    val nationalId: String? = null,
    @ColumnInfo("is_house_hold_head")
    val isHouseholdHead: Boolean = false,
    @ColumnInfo("assignHousehold")
    val assignHousehold: Int? = null,
    @ColumnInfo("guardian_hh_member_id")
    val guardianId: Long? = null,
    @ColumnInfo("guardian_hh_member_fhir_id")
    val guardianFhirId: String? = null,
    @ColumnInfo("marital_status")
    val maritalStatus: String? = null,
    @ColumnInfo("disability")
    val disability: String? = null,
) {
    @Ignore
    var isChild: Boolean? = false

    @Ignore
    var provenance: ProvanceDto = ProvanceDto(modifiedDate = updatedAt.convertToUtcDateTime())

    @Ignore
    var assessments = listOf<Assessment>()

    @Ignore
    var rxBuddies = listOf<RxBuddy>()

    @Ignore
    var children: List<HouseHoldMember>? = null

    fun toHouseholdMemberEntity(
        hhId: Long?,
        status: OfflineSyncStatus,
        id: Long = 0,
    ): HouseholdMemberEntity =
        HouseholdMemberEntity(
            id = id,
            name = this.name,
            phoneNumber = this.phoneNumber,
            dateOfBirth = this.dateOfBirth,
            gender = this.gender,
            householdId = hhId,
            patientId = this.patientId,
            villageId = this.villageId,
            isActive = this.isActive,
            version = this.version,
            lastUpdated = this.lastUpdated,
            motherReferenceId = this.motherReferenceId?.toLongOrNull(),
            deceasedReason = this.deceasedReason,
            latitude = this.latitude,
            longitude = this.longitude,
            idType = this.idType ?: "",
            nationalId = this.nationalId,
            isHouseholdHead = this.isHouseholdHead,
            householdFhirId = this.householdId,
            guardianId = this.guardianId,
            guardianFhirId = this.guardianFhirId,
            maritalStatus = this.maritalStatus,
            disability = this.disability,
        ).apply {
            fhirId = this@HouseHoldMember.id.toString()
            sync_status = status
            createdAt = this@HouseHoldMember.createdAt
            updatedAt = this@HouseHoldMember.updatedAt
        }
}
