package org.medtroniclabs.uhis.model

import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.data.CodeDetailsObject
import org.medtroniclabs.uhis.data.EncounterDetails

data class LabTestCreateRequest(
    val encounter: EncounterDetails,
    val labTests: ArrayList<LabTestDetails>,
    val enrollmentType: String? = null,
    val identityValue: String? = null,
    val requestFrom: String = CommonUtils.requestFrom(),
)

data class LabTestDetails(
    val testName: String,
    val labTestId: Long? = null,
    val recommendedBy: String,
    val recommendedName: String? = null,
    val recommendedOn: String,
    val codeDetails: CodeDetailsObject? = null,
    val labTestResults: ArrayList<LabTestResultObject>? = null,
    val id: String? = null,
)

data class LabTestResultObject(
    var name: String,
    var value: Any? = null,
    var performedBy: String,
    val codeDetails: CodeDetailsObject? = null,
    val testedOn: String? = null,
    var resource: String? = null,
    var unit: String? = null,
)
