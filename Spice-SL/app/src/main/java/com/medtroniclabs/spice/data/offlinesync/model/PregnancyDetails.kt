package com.medtroniclabs.spice.data.offlinesync.model

data class PregnancyDetails(
    val householdMemberId: String,
    val ancVisitNo: Int?,
    val lastMenstrualPeriod: String?,
    val pncVisitNo: Int?,
    val dateOfDelivery: String?,
    val noOfNeonates: Int?,
    val childVisitNo: Int?
)
