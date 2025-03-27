package com.medtroniclabs.spice.data.offlinesync.model

import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.medtroniclabs.spice.appextensions.convertToUtcDateTime
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.data.offlinesync.utils.OfflineSyncStatus
import java.util.Locale

data class HouseHold(

    @ColumnInfo(name = "id")
    var referenceId: String?,

    @ColumnInfo(name = "fhir_id")
    var id: String? = null,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name= "villageName")
    val village: String,

    @ColumnInfo(name = "village_id")
    val villageId: Long,

    @ColumnInfo(name = "landmark")
    val landmark: String? = null,

    @ColumnInfo(name = "head_phone_number")
    val headPhoneNumber: String,

    @ColumnInfo(name = "head_phone_number_category")
    val headPhoneNumberCategory: String? = null,

    @ColumnInfo(name = "no_of_people")
    val noOfPeople: Int,

    @ColumnInfo(name = "household_no")
    val householdNo: Long? = null,

    @ColumnInfo(name = "is_owned_hand_washing_facility_with_soap")
    val ownedHandWashingFacilityWithSoap: Boolean,

    @ColumnInfo(name = "is_owned_a_treated_bed_net")
    val ownedTreatedBedNet: Boolean,

    @ColumnInfo(name = "is_owned_an_improved_latrine")
    val ownedAnImprovedLatrine: Boolean,

    @ColumnInfo(name = "has_improved_water_source")
    val hasImprovedWaterSource: Boolean,

    @ColumnInfo(name = "bed_net_count")
    val bedNetCount: Int? = null,

    @ColumnInfo(name = "latitude")
    val latitude: Double = 0.0,

    @ColumnInfo(name = "longitude")
    val longitude: Double = 0.0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    val version: String? = null,

    val lastUpdated: String? = null
) {
    @Ignore
    var provenance: ProvanceDto = ProvanceDto(modifiedDate = updatedAt.convertToUtcDateTime())

    @Ignore
    var householdMembers: MutableList<HouseHoldMember> = mutableListOf()

    fun toHouseholdEntity(status: OfflineSyncStatus = OfflineSyncStatus.Success, id: Long = 0): HouseholdEntity {
        return HouseholdEntity(
            id = id,
            householdNo = this.householdNo,
            name = this.name,
            villageId = this.villageId,
            landmark = this.landmark,
            headPhoneNumber = this.headPhoneNumber,
            headPhoneNumberCategory = toRegularCase(this.headPhoneNumberCategory),
            noOfPeople = this.noOfPeople,
            isOwnedAnImprovedLatrine = this.ownedAnImprovedLatrine,
            hasImprovedWaterSource = this.hasImprovedWaterSource,
            isOwnedHandWashingFacilityWithSoap = this.ownedHandWashingFacilityWithSoap,
            isOwnedATreatedBedNet = this.ownedTreatedBedNet,
            bedNetCount = this.bedNetCount,
            latitude = this.latitude,
            longitude = this.longitude,
            version = this.version,
            lastUpdated = this.lastUpdated).apply {
                fhirId = this@HouseHold.id.toString()
                sync_status = status
        }
    }

    private fun toRegularCase(sentence: String?): String {
        return sentence?.split(" ")?.joinToString(" ") { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
            ?: ""
    }
}
