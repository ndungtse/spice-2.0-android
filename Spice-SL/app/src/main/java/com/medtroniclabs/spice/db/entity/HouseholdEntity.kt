package com.medtroniclabs.spice.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "household")
data class HouseholdEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "household_no")
    val householdNo: Long,
    val name: String,
    @ColumnInfo(name= "village_id")
    val villageId:Long,
    val landmark:String,
    @ColumnInfo(name="head_phone_number")
    val headPhoneNumber:String,
    @ColumnInfo(name= "no_of_people")
    val noOfPeople: Int,
    @ColumnInfo("is_owned_an_improved_latrine")
    val isOwnedAnImprovedLatrine:Boolean,
    @ColumnInfo("is_owned_hand_washing_facility_with_soap")
    val isOwnedHandWashingFacilityWithSoap:Boolean,
    @ColumnInfo("is_owned_a_treated_bed_net")
    val isOwnedATreatedBedNet:Boolean,
    @ColumnInfo("bed_net_count")
    val bedNetCount:Int? = null,
    val noOfPeopleRegistered: Int? = null,
 )

