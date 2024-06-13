package com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.listener

import com.medtroniclabs.spice.model.PatientListRespModel

interface PncVisitCallBack {
    fun onDataLoaded(data: PatientListRespModel)
}