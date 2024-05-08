package com.medtroniclabs.spice.data

data class MedicationResponse(
    val id: String,
    val name: String,
    val classificationId: Long,
    val dosageFormId:Long,
    val brandId:Long,
    val classificationName:String,
    val brandName:String,
    val dosageFormName:String,
    val countryId:Long,
    val tenantId:Long,
    var selectedMap: HashMap<String, Any>?,
    var quantity: Long,
    var prescribedDays: Long?
)

data class MedicationRequestObject(val medicationResponse: MedicationResponse)