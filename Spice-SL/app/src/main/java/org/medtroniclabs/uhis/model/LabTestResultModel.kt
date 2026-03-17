package org.medtroniclabs.uhis.model

data class LabTestResultModel(
    val labTestName: String? = null,
    val resultValue: String? = null,
    val labTestUom: String? = null,
    val normalRange: String? = null,
    val isHeader: Boolean,
)
