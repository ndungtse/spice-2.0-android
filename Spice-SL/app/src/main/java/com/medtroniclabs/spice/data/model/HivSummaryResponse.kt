package com.medtroniclabs.spice.data.model

import com.medtroniclabs.spice.data.DiagnosisDiseaseModel
import com.medtroniclabs.spice.data.Prescription
import com.medtroniclabs.spice.data.history.Investigation
import com.medtroniclabs.spice.data.history.PatientStatus

data class HivSummaryResponse(
    val id: String? = null,
    val patientStatus: String? = null,
    val diagnosis: List<DiagnosisDiseaseModel>? = null,
    val presentingComplaints: List<String>? = null,
    val presentingComplaintsNotes: String? = null,
    val systemicExaminations: Map<String, String>? = null,
    val comorbiditiesCoinfections: List<String>? = null,
    val obstetricExaminations: List<String>? = null,
    val obstetricExaminationNotes:String? = null,
    val comorbiditiesCoinfectionsNotes: String? = null,
    val prescriptions: List<Prescription>? = null,
    val investigations: List<Investigation>? = null,
    val summaryStatus: List<PatientStatus>? = null,
    val clinicalNotes: String? = null
)
