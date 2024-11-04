package com.medtroniclabs.spice.model

import com.medtroniclabs.spice.model.medicalreview.SearchLabTestResponse

data class LabTestListResponse(
    val id: String,
    val testName: String,
    val recommendedBy: String,
    val recommendedOn: String,
    val recommendedName: String,
    val labTestResults: ArrayList<LabTestResultObject>,
    val labTestCustomization: SearchLabTestResponse,
    val testedOn: String? = null
)