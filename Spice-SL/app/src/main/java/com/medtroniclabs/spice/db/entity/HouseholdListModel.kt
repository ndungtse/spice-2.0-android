package com.medtroniclabs.spice.db.entity

data class HouseholdListModel(
    val id: Long,
    val householdName: String,
    val householdNo: Long,
    val noOfPeople: Int,
    val noOfPeopleRegistered: Int
)
