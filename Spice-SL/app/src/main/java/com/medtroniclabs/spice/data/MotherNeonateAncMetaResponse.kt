package com.medtroniclabs.spice.data

data class MotherNeonateAncMetaResponse(
    val presentingComplaints: List<ExaminationsComplaintItems>,
    val obstetricExaminations: List<ExaminationsComplaintItems>,
    val pregnancyHistories: List<ExaminationsComplaintItems>,
    val bloodGroup: List<ExaminationsComplaintItems>
)
