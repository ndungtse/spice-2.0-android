package com.medtroniclabs.spice.data

data class PregnancyDetailsModel(
    var height: Double? = null,
    var weight: Double? = null,
    var pulse: Int? = null,
    var lastMenstrualPeriod: String? = null,
    var estimatedDeliveryDate: String? = null,
    var gestationalAge: Long? = null,
    var noOfFetus: Int? = null,
    var gravida: Int? = null,
    var parity: Int? = null,
    var patientBloodGroup: String? = null,
    var bmi: Double? = null,
    var systolic: Double? = null,
    var diastolic: Double? = null,
)
