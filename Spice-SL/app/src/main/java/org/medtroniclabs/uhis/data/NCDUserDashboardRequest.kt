package org.medtroniclabs.uhis.data

data class NCDUserDashboardRequest(
    val sortField: String? = null,
    val customDate: CustomDateModel? = null,
    val userId: String? = null,
    val filterBySs: List<Long>? = null,
    val filterBySubVillages: List<Long>? = null,
)

data class CustomDateModel(val startDate: String? = null, val endDate: String? = null)

data class NCDUserDashboardResponse(
    val screened: Int? = null,
    val assessed: Int? = null,
    val registered: Int? = null,
    val referred: Int? = null,
    val dispensed: Int? = null,
    val investigated: Int? = null,
    val nutritionistLifestyleCount: Int? = null,
    val psychologicalNotesCount: Int? = null,
    val familyPlanningCount: Int? = null,
    val pregnantWomenRegistrationCount: Int? = null,
    val pregnancyOutcomeCount: Int? = null,
    val ancCount: Int? = null,
    val pncCount: Int? = null,
    val childVisitCount: Int? = null,
    val tbAssessmentCount: Int? = null,
    val tbContactTracingCount: Int? = null,
    val eyeCareCount: Int? = null,
    val cataractCount: Int? = null,
    val householdRegisteredCount: Int? = null,
    val pwIdentifiedFirst4MonthsWithAncCount: Int? = null,
    val anc3PlusCount: Int? = null,
    val highRiskPregnantWomenCount: Int? = null,
)

data class ActivityModel(
    val key: String,
    val title: String,
    val fieldName: String,
)
