package com.medtroniclabs.spice.data

import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto

data class PregnancyDetailsModel(
    var height: Double? = null,
    var weight: Double? = null,
    var pulse: Double? = null,
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
    var isPregnant: Boolean? = null,
    var isOnTreatment: Boolean? = null,
    var isPregnancyAnc: Boolean? = null,
    var relatedPersonFhirId: String? = null,
    var diagnosis: ArrayList<Map<String, Any>>? = null,
    var actualDeliveryDate: String? = null,
    var diagnosisTime: String? = null,
    var maternalOutcomes: String? = null,
    var neonatalOutcomes: String? = null,
    var temperature: Int? = null,
    val provenance: ProvanceDto = ProvanceDto(),
)
