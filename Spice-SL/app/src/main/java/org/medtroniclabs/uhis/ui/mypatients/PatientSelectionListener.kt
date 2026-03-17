package org.medtroniclabs.uhis.ui.mypatients

import org.medtroniclabs.uhis.db.entity.NCDFollowUp
import org.medtroniclabs.uhis.model.PatientListRespModel
import org.medtroniclabs.uhis.ncd.data.PatientFollowUpEntity

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
