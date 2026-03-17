package org.medtroniclabs.uhis.data.model

import org.medtroniclabs.uhis.data.DiagnosisDiseaseModel
import org.medtroniclabs.uhis.data.Prescription
import org.medtroniclabs.uhis.data.history.Investigation
import org.medtroniclabs.uhis.data.history.PatientStatus

data class HivSummaryResponse(
    val id: String? = null,
    val patientStatus: String? = null,
    val diagnosis: List<DiagnosisDiseaseModel>? = null,
    val presentingComplaints: List<String>? = null,
    val presentingComplaintsNotes: String? = null,
    val systemicExaminations: Map<String, String>? = null,
    val comorbiditiesCoinfections: List<String>? = null,
    val obstetricExaminations: List<String>? = null,
    val obstetricExaminationNotes: String? = null,
    val comorbiditiesCoinfectionsNotes: String? = null,
    val prescriptions: List<Prescription>? = null,
    val investigations: List<Investigation>? = null,
    val summaryStatus: List<PatientStatus>? = null,
    val clinicalNotes: String? = null,
    val ancVisitNumber: Int? = null,
    val emtctVisitStatus: String? = null,
)
