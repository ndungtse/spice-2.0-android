package org.medtroniclabs.uhis.db.response

import androidx.room.ColumnInfo

data class HouseHoldEntityWithLastActivity(
    val id: Long,
    @ColumnInfo("household_no")
    val householdNo: String? = null,
    val name: String,
    @ColumnInfo("village_name")
    val villageName: String,
    @ColumnInfo("sub_village_name")
    val subVillageName: String,
    @ColumnInfo("shasthya_shebika_name")
    val shasthyaShebikaName: String,
    /** Epoch-ms of the most recent member registration or assessment, whichever is later. */
    @ColumnInfo("last_activity_at")
    val lastActivityAt: Long,
    /** Epoch-ms of the most recently registered household member. */
    @ColumnInfo("last_member_registered_at")
    val lastMemberRegisteredAt: Long,
)
