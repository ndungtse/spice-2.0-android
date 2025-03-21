package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = EntitiesName.RX_BUDDY_DETAILS)
data class RxBuddyDetails(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var rxBuddyId:Long?=null,
    var patientMemberId: String,
    var memberId:String?=null,
    var name: String?=null,
    var phoneNumber: String?=null,
    var relationship: String,
    var isMonitorSheetProvider: Boolean,
)
