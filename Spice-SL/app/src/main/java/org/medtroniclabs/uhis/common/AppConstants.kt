package org.medtroniclabs.uhis.common

object AppConstants {
    const val SHA_MAC = "HmacSHA512"

    const val CLIENT_CONSTANT = "mob"

    const val ANDROID = "Android"

    const val CONFLICT_ERROR_CODE = 409

    val exemptionList = listOf("HouseholdActivity", "AssessmentActivity", "FollowUpMyPatientActivity", "ScreeningActivity")

    const val IS_DIFFERENT_LOGIN = "isDifferentLogin"

    const val FLAVOUR_PROD = "production"
}
