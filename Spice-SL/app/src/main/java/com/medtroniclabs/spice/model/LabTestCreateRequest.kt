package com.medtroniclabs.spice.model

import com.medtroniclabs.spice.data.CodeDetailsObject
import com.medtroniclabs.spice.data.EncounterDetails

data class LabTestCreateRequest(
    val encounter: EncounterDetails,
    val labTests: ArrayList<LabTestDetails>,
    val type: String? = null
)

data class LabTestDetails(
    val testName: String,
    val recommendedBy: String,
    val recommendedName: String? = null,
    val recommendedOn: String,
    val codeDetails: CodeDetailsObject? = null,
    val labTestResults: ArrayList<LabTestResultObject>? = null,
    val id: String? = null
)

data class LabTestResultObject(
    var name: String,
    var value: Any? = null,
    var performedBy: String,
    val codeDetails: CodeDetailsObject? = null,
    val testedOn: String? = null,
    var resource: String? = null,
    var unit: String? = null
)