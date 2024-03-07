package com.medtroniclabs.spice.model

import androidx.room.ColumnInfo

data class MemberDobGenderModel(
    val gender: String,
    @ColumnInfo("date_of_birth")
    val dateOfBirth: String
)