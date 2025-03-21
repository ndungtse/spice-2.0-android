package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = EntitiesName.RX_BUDDY_FOLLOW_UP_ENTITY)
data class RxBuddyFollowUpEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var rxBuddyId:Long?=null,
    var patientMemberId: String,
    var rxBuddyMonitoringSheetDate:String,
    var isAnyOfSymptomsWorse: Boolean,
    var isAnyOfMedicationNeeded: Boolean,
)