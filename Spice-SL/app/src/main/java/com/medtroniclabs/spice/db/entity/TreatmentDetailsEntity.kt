package com.medtroniclabs.spice.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = EntitiesName.TREATMENT_DETAILS_ENTITY)
data class TreatmentDetailsEntity(
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0,
    var memberId: String,
    var type:String,
    var diagnoses:String,
    var diagnosedDate:String,
    var treatmentStartDate:String,
    var healthUnitNo:Long,
    var icDistrictTBNo:Long,
    var typeOfDrug:String,
    var noOfTabletsGivenForTB:Long
)
