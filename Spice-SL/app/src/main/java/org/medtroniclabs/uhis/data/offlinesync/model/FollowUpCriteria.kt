package org.medtroniclabs.uhis.data.offlinesync.model

data class FollowUpCriteria(
    val malaria: Int,
    val pneumonia: Int,
    val diarrhea: Int,
    val muac: Int,
    val escalation: Int,
    val referral: Int,
    val ancVisit: Int,
    val pncVisit: Int,
    val childVisit: Int,
    val successfulAttempts: Int,
    val unsuccessfulAttempts: Int,
    val informedCallAttempts: Int,
    val notInformedCallAttempts: Int,
)
