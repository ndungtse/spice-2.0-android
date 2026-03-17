package org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc

import org.medtroniclabs.uhis.model.PatientListRespModel

interface AncVisitCallBack {
    fun onDataLoaded(details: PatientListRespModel)
}
