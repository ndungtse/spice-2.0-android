package com.medtroniclabs.spice.ui.assessment.referrallogic

/**
 * Enum class representing the danger signs that can occur after childbirth.
 * These signs are used for assessment during postnatal care (PNC).
 */
enum class PostpartumDangerSigns(val value: String) {
    HEAVY_BLEEDING("heavyBleeding"),
    FOUL_SMELLING_DISCHARGE("foulSmellingDischarge"),
    SEVERE_ABDOMINAL_PAIN("severeAbdominalPain"),
    SEVERE_HEADACHE_VISION_CONVULSIONS("severeHeadacheVisionConvulsions"),
    PERINEAL_WOUND_DISCHARGE("perinealWoundDischarge"),
    BREAST_PAIN_SWELLING_FEVER("breastPainSwellingFever"),
    OTHER("other"),
}

/**
 * Enum class representing the urgent referral conditions for postnatal care.
 */
enum class PNCUrgentReferrals(val value: String) {
    HEAVY_BLEEDING("Heavy bleeding"),
    SEVERE_ABDOMINAL_PAIN("Severe abdominal pain"),
    SEVERE_HEADACHE_VISUAL_ISSUES_CONVULSIONS("Severe headache/visual issues/convulsions"),
    FOUL_SMELLING_DISCHARGE("Foul-smelling discharge"),
    PERINEUM_TEAR_DISCHARGE("Perineum tear / Discharge from wound area"),
    HIGH_BP("High BP"),
    HTN_ECLAMPSIA_NOT_ON_TREATMENT("Not on treatment for HTN or Pre-eclampsia /Eclampsia"),
    EDEMA_PLUS_RISK("Edema"),
    URINE_ALBUMIN_PLUS_RISK("Urine Albumin"),
    HIGH_FEVER("High Fever"),
    ABNORMAL_PULSE("Abnormal Pulse"),
    SEVERE_ANEMIA("Severe Anemia"),
    HIGH_BLOOD_SUGAR("High Blood sugar"),
    DM_GDM_NOT_ON_TREATMENT("Known DM/GDM patient not on treatment"),
    SUSPECTED_JAUNDICE("Suspected Jaundice"),
}

/**
 * Enum class representing the non-urgent referral conditions for postnatal care.
 */
enum class PNCNonUrgentReferral(val value: String) {
    MODERATE_ANEMIA("Moderate Anemia"),
    MILD_ANEMIA("Mild Anemia"),
    BREAST_ISSUES("Cracked nipples / painful / swollen breasts with or without fever"),
    FEVER("Fever"),
    HTN_ECLAMPSIA_ON_TREATMENT("On treatment for HTN or Pre-eclampsia / Eclampsia"),
    DM_GDM_ON_TREATMENT("On treatment for DM/GDM"),
    OTHER("Other"),
}

/**
 * Enum class representing the care gaps identified during postnatal care assessment.
 */
enum class PNCGaps(val value: String) {
    SUPPLEMENTATION("Supplementation"),
    CONTRACEPTION_GAP("Not using postpartum contraception"),
}

/**
 * Enum class representing the specific supplements for postnatal care.
 */
enum class PNCSupplementation(val value: String) {
    VITAMIN_A("Vitamin A"),
    IFA("IFA"),
    CALCIUM("Calcium"),
}

/**
 * PNC referral type
 */
enum class PNCReferralType {
    URGENT,
    NON_URGENT,
    NONE,
}

/**
 * Anemia level
 */
enum class AnemiaLevel {
    Moderate, // if Hb<10
    Severe, // if Hb <8
    Mild, // if Hb <11
    None,
}
