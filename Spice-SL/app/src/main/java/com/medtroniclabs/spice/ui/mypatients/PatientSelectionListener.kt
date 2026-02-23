package com.medtroniclabs.spice.ui.mypatients

import com.medtroniclabs.spice.db.entity.NCDFollowUp
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.ncd.data.PatientFollowUpEntity

interface PatientSelectionListener {
    fun onSelectedPatient(item: PatientListRespModel)
}

interface PatientSelectionListenerForFollowUp {
    fun onSelectedPatientForCall(item: PatientFollowUpEntity)

    fun onSelectedPatientForAssessment(item: PatientFollowUpEntity)

    fun onSelectedPatientCard(item: PatientFollowUpEntity)
}

interface PatientSelectionListenerForFollowUpOffline {
    fun onSelectedPatientForCall(item: NCDFollowUp)

    fun onSelectedPatientForAssessment(item: NCDFollowUp)

    fun onSelectedPatientCard(item: NCDFollowUp)
}
