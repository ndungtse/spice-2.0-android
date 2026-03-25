package org.medtroniclabs.uhis.db.response

data class MaternalDashboardCountsRow(
    val pwIdentifiedFirst4MonthsWithAncCount: Int?,
    val anc3PlusCount: Int?,
    val highRiskPregnantWomenCount: Int?,
)
