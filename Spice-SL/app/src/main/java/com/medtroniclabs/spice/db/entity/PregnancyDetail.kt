package com.medtroniclabs.spice.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = EntitiesName.PREGNANCY_DETAIL)
data class PregnancyDetail(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var patientId: String? = null,
    var householdMemberId: String? = null,
    // Anc Details
    var ancVisitNo: Long? = null,
    var lastMenstrualPeriod: String? = null,
    var estimatedDeliveryDate: String? = null,
    // Pnc Detail
    var pncVisitNo: Long? = null,
    var dateOfDelivery: String? = null,
    var noOfNeonates: Long? = null,
    //Childhood Visit Detail
    var childVisitNo: Long? = null,
)
