package com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc

import com.medtroniclabs.spice.model.PatientListRespModel

interface AncVisitCallBack {
    fun onDataLoaded(details: PatientListRespModel)
}
