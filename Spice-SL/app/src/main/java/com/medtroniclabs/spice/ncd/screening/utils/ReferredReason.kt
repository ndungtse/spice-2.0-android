package com.medtroniclabs.spice.ncd.screening.utils

import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.data.Country
import com.medtroniclabs.spice.ncd.data.ValidatePatientModel

object ReferredReason {
    const val bloodPressure = "High BP"
    const val PHQ4 = "PHQ4"
    const val bloodGlucose = "High BG"
    const val pregnancySymptoms = "PREGNANCY-SYMPTOMS"
    const val SuicidalIdeation = "Suicidal-Ideation"
    const val CAGEAID = "CAGEAID"

    fun validateRequest(map: HashMap<String, Any>): ValidatePatientModel {
        return ValidatePatientModel(
            identityType = map[Screening.identityType]?.toString(),
            identityValue = map[Screening.identityValue]?.toString(),
            firstName = map[Screening.firstName]?.toString(),
            lastName = map[Screening.lastName]?.toString(),
            phoneNumber = map[Screening.phoneNumber]?.toString(),
            country = Country(id = SecuredPreference.getCountryId())
        )
    }
}