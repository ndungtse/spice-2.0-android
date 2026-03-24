package org.medtroniclabs.uhis.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.medtroniclabs.uhis.db.entity.EntitiesName.ASSESSMENT
import org.medtroniclabs.uhis.ui.assessment.referrallogic.utils.ReferralStatus

@Entity(
    tableName = ASSESSMENT,
    indices = [
        Index(value = ["householdMemberLocalId"], name = "idx_householdMemberLocalId"),
        Index(value = ["updated_at"], name = "idx_Assessment_updated_at"),
    ],
)
data class AssessmentEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val householdMemberLocalId: Long = 0,
    val memberId: String?, // Member -FHIR id
    val householdId: String?, // Household - FHIR id
    val patientId: String?, // member - patient id
    val villageId: String, // Sub-Village id of household
    val assessmentType: String,
    var assessmentDetails: String,
    var otherDetails: String? = null,
    var callResult: String? = null,
    var isReferred: Boolean = false,
    val referralStatus: ReferralStatus,
    val referredReason: ArrayList<String>? = null,
    val followUpId: Long? = null,
    val status: ArrayList<String>? = null,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
) : BaseEntity()
