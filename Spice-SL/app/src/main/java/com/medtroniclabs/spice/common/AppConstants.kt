package com.medtroniclabs.spice.common

object AppConstants {

    @JvmStatic
    val SHA_MAC = "HmacSHA512"

    val CLIENT_CONSTANT = "mob"

    val exemptionList = listOf("HouseholdActivity", "AssessmentActivity", "FollowUpMyPatientActivity","ScreeningActivity")
}