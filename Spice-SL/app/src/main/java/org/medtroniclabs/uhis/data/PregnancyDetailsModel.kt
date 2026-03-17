package org.medtroniclabs.uhis.data

import org.medtroniclabs.uhis.data.offlinesync.model.ProvanceDto
import org.medtroniclabs.uhis.ncd.data.NcdPatientStatus

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
    var memberReference: String? = null,
    var patientReference: String? = null,
    var diagnosis: ArrayList<Map<String, Any>>? = null,
    var actualDeliveryDate: String? = null,
    var diagnosisTime: String? = null,
    var maternalOutcomes: String? = null,
    var neonatalOutcomes: String? = null,
    var temperature: Double? = null,
    var ncdPatientStatus: NcdPatientStatus? = null,
    var provenance: ProvanceDto = ProvanceDto(),
    var patientVisitId: String? = null,
    var pregnancyHistory: List<String>? = null,
    var pregnancyHistoryNotes: String? = null,
)
