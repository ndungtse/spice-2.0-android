package com.medtroniclabs.spice.ui.mypatients

import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.data.PatientFollowUpEntity

interface PatientSelectionListener {
    fun onSelectedPatient(item: PatientListRespModel)
}

interface PatientSelectionListenerForFollowUp {
    fun onSelectedPatientForCall(item: PatientFollowUpEntity)
    fun onSelectedPatientForAssessment(item: PatientFollowUpEntity)
}
