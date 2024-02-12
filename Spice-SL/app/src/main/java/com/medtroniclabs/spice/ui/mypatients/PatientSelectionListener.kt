package com.medtroniclabs.spice.ui.mypatients

import com.medtroniclabs.spice.model.PatientListRespModel

interface PatientSelectionListener {
    fun onSelectedPatient(item: PatientListRespModel)
}