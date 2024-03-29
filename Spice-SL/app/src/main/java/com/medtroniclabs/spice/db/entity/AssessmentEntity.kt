package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.db.entity.EntitiesName.ASSESSMENT

@Entity(tableName = ASSESSMENT)
data class AssessmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val householdId: Long,
    val patientId: String,
    val assessmentType: String,
    val assessmentDetails:String,
    var otherDetails: String? = null,
    var createdAt: Long = System.currentTimeMillis(),
    var userId: Long? = null,
    var isReferred: Boolean = false,
    val referralStatus: ReferralStatus,
    val referredReason: ArrayList<String>?= null
)
