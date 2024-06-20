package com.medtroniclabs.spice.data

data class MedicationResponse(
    val id: String,
    val name: String,
    val classificationName: String,
    val brandName: String,
    val dosageFormName: String,
    var selectedMap: HashMap<String, Any>?,
    var quantity: Long,
    var prescribedDays: Long?,
    var prescriptionId: String? = null,
    var isEditable: Boolean = false,
    var prescribedSince:String ?= null,
    var showErrorMessage: Boolean = false
)

data class MedicationRequestObject(var medicationResponse: MedicationResponse)