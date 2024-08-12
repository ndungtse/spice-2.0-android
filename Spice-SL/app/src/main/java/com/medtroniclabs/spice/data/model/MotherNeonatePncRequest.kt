import com.medtroniclabs.spice.data.model.MedicalReviewEncounter


data class MotherNeonatePncRequest(
    var pncMother: PncMother? = null,
    var pncChild: PncChild? = null
)


data class PncMother(
    var isMotherAlive: Boolean? = null,
    var breastCondition: String? = null,
    var breastConditionNotes: String? = null,
    var involutionsOfTheUterus: String? = null,
    var involutionsOfTheUterusNotes: String? = null,
    var presentingComplaints: List<String?>? = null,
    var presentingComplaintsNotes: String? = null,
    var systemicExaminations: List<String?>? = null,
    var systemicExaminationsNotes: String? = null,
    var clinicalNotes: String? = null,
    var encounter: MedicalReviewEncounter? = null
)


data class PncChild(
    var isChildAlive: Boolean? = null,
    var breastFeeding: Boolean? = null,
    var exclusiveBreastFeeding: Boolean? = null,
    var cordExamination: String? = null,
    var congenitalDetect: String? = null,
    var presentingComplaints: List<String?>? = null,
    var presentingComplaintsNotes: String? = null,
    var physicalExaminations: List<String?>? = null,
    var physicalExaminationsNotes: String? = null,
    var clinicalNotes: String? = null,
    var encounter: MedicalReviewEncounter? = null
)