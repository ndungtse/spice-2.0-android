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
}

/**
 * Enum class representing the urgent referral conditions for postnatal care.
 */
enum class PNCUrgentReferrals(val value: String) {
    HEAVY_BLEEDING("Heavy bleeding (>5 pads per day)"),
    SEVERE_ABDOMINAL_PAIN("Severe abdominal pain"),
    SEVERE_HEADACHE_VISUAL_ISSUES_CONVULSIONS("Severe headache/visual issues/convulsions"),
    FOUL_SMELLING_DISCHARGE("Foul-smelling discharge"),
    PERINEUM_TEAR_DISCHARGE("Perineum tear / Discharge from wound area"),
    BP_GE_140_90_HTN_ECLAMPSIA_NOT_ON_TREATMENT("BP ≥140/90 OR known HTN or PW with eclampsia not on treatment"),
    EDEMA_PLUS_BP_GE_140_90_HTN_ALBUMIN("Edema (+ BP≥140/90 OR known HTN or Urine Albumin present)"),
    URINE_ALBUMIN_POSITIVE_PLUS_RISK("Urine Albumin Positive + (BP≥140/90 OR known HTN or Edema present)"),
    HIGH_FEVER("High Fever - >=102°F"),
    PULSE_OUT_OF_RANGE("Pulse If >90 or <60"),
    SEVERE_ANEMIA("Severe Anemia (Hb <8 g/dL)"),
    SUGAR_DM_GDM_NOT_ON_TREATMENT("Fasting sugar ≥7mmol/L or Random sugar ≥11.1 mmol/L Or known DM/GDM patient not on treatment"),
    URINARY_BILIRUBIN_PRESENT("Urinary Bilirubin present"),
}

/**
 * Enum class representing the non-urgent referral conditions for postnatal care.
 */
enum class PNCNonUrgentReferral(val value: String) {
    HB_MODERATE_ANEMIA("Hb 8–10 (moderate anemia)"),
    BREAST_ISSUES("Cracked nipples / painful / swollen breasts with or without fever"),
    FEVER("Fever - >=100°F"),
    HTN_ECLAMPSIA_ON_TREATMENT("Known HTN patient or PW with eclampsia on treatment"),
    DM_GDM_ON_TREATMENT("Known DM or GDM patient on treatment"),
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
