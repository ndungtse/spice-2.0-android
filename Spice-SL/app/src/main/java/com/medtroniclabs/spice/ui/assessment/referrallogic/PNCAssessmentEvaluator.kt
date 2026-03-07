package com.medtroniclabs.spice.ui.assessment.referrallogic

import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH

/**
 * Evaluator class for Postnatal Care (PNC) assessments.
 * This class provides methods to identify urgent referrals, non-urgent referrals,
 * and care gaps based on assessment results.
 */
object PNCAssessmentEvaluator {
    /**
     * Identifies urgent referral conditions from the maternal health assessment data.
     * The logic follows a strict sequence of checks including danger signs, BP, fever,
     * pulse, and hemoglobin levels.
     *
     * @param resultMap The assessment result map containing maternal health data.
     * @return A list of strings representing the urgent referral reasons found.
     */
    fun getUrgentReferral(resultMap: Map<String, Any>): List<String> {
        val urgentReferral = arrayListOf<String>()

        (resultMap[RMNCH.ID_MATERNAL_HEALTH_ASSESSMENT] as? Map<*, *>)?.let { maternalAssessment ->
            (maternalAssessment[RMNCH.ID_POSTPARTUM_DANGER_SIGNS] as? List<*>)?.let { dangerSigns ->
                val selectedSigns = dangerSigns
                    .filterIsInstance<Map<String, Any>>()
                    .mapNotNull {
                        it[DefinedParams.Value] as? String
                    }.toSet()

                // Danger Signs (1-5)
                // 1. Heavy bleeding (>5 pads per day)
                if (selectedSigns.contains(PostpartumDangerSigns.HEAVY_BLEEDING.value)) {
                    urgentReferral.add(PNCUrgentReferrals.HEAVY_BLEEDING.value)
                }

                // 2. Severe abdominal pain
                if (selectedSigns.contains(PostpartumDangerSigns.SEVERE_ABDOMINAL_PAIN.value)) {
                    urgentReferral.add(PNCUrgentReferrals.SEVERE_ABDOMINAL_PAIN.value)
                }

                // 3. Severe headache/visual issues/convulsions
                if (selectedSigns.contains(PostpartumDangerSigns.SEVERE_HEADACHE_VISION_CONVULSIONS.value)) {
                    urgentReferral.add(PNCUrgentReferrals.SEVERE_HEADACHE_VISUAL_ISSUES_CONVULSIONS.value)
                }

                // 4. Foul-smelling discharge
                if (selectedSigns.contains(PostpartumDangerSigns.FOUL_SMELLING_DISCHARGE.value)) {
                    urgentReferral.add(PNCUrgentReferrals.FOUL_SMELLING_DISCHARGE.value)
                }

                // 5. Perineum tear / Discharge from wound area
                if (selectedSigns.contains(PostpartumDangerSigns.PERINEAL_WOUND_DISCHARGE.value)) {
                    urgentReferral.add(PNCUrgentReferrals.PERINEUM_TEAR_DISCHARGE.value)
                }
            }

            val systolic = CommonUtils.getInteger(maternalAssessment[RMNCH.ID_SYSTOLIC])
            val diastolic = CommonUtils.getInteger(maternalAssessment[RMNCH.ID_DIASTOLIC])
            val isBpHigh = isBpHigh(systolic, diastolic)
            val isKnownHtn = isSelectionPresent(maternalAssessment[RMNCH.ID_KNOWN_HTN], DefinedParams.Yes)
            val isEclampsia = isSelectionPresent(maternalAssessment[RMNCH.ID_ECLAMPSIA], DefinedParams.Yes)
            val isOnTreatment = isSelectionPresent(maternalAssessment[RMNCH.ID_ON_TREATMENT_HTN_ECLAMPSIA], DefinedParams.Yes)
            val isEdemaPresent = isSelectionPresent(maternalAssessment[RMNCH.ID_EDEMA])
            val isUrineAlbuminPresent = isSelectionPresent(maternalAssessment[RMNCH.ID_URINARY_ALBUMIN])

            // 6. BP ≥140/90 OR (known HTN or PW with eclampsia) and NOT on treatment
            if (isBpHigh || ((isKnownHtn || isEclampsia) && !isOnTreatment)) {
                urgentReferral.add(PNCUrgentReferrals.BP_GE_140_90_HTN_ECLAMPSIA_NOT_ON_TREATMENT.value)
            }

            // 7. Edema (+ BP≥140/90 OR known HTN or Urine Albumin present)
            if (isEdemaPresent && (isBpHigh || isKnownHtn || isUrineAlbuminPresent)) {
                urgentReferral.add(PNCUrgentReferrals.EDEMA_PLUS_BP_GE_140_90_HTN_ALBUMIN.value)
            }

            // Severe Anemia
            CommonUtils.getDoubleOrNull(maternalAssessment[RMNCH.ID_HEMOGLOBIN])?.takeIf { it > 0 }?.let { hb ->
                if (hb < AssessmentDefinedParams.HEMOGLOBIN_SEVERE_ANEMIA_THRESHOLD) {
                    urgentReferral.add(PNCUrgentReferrals.SEVERE_ANEMIA.value)
                }
            }

            // 8. Urine Albumin Positive + (BP≥140/90 OR known HTN or Edema present)
            if (isUrineAlbuminPresent && (isBpHigh || isKnownHtn || isEdemaPresent)) {
                urgentReferral.add(PNCUrgentReferrals.URINE_ALBUMIN_POSITIVE_PLUS_RISK.value)
            }

            // 10. High Fever - >=102°F
            CommonUtils.getDoubleOrNull(maternalAssessment[RMNCH.ID_TEMPERATURE])?.takeIf { it > 0 }?.let { temp ->
                if (temp >= 102.0) {
                    urgentReferral.add(PNCUrgentReferrals.HIGH_FEVER.value)
                }
            }

            // 11. Pulse If >90 or <60
            val pulse = CommonUtils.getInteger(maternalAssessment[RMNCH.ID_PULSE])
            if (pulse > 0 && pulse !in 60..90) {
                urgentReferral.add(PNCUrgentReferrals.PULSE_OUT_OF_RANGE.value)
            }

            // 12. Severe Anemia (Hb <8 g/dL)
            CommonUtils.getDoubleOrNull(maternalAssessment[RMNCH.ID_HEMOGLOBIN])?.takeIf { it > 0 }?.let { hb ->
                if (hb < 8.0) {
                    urgentReferral.add(PNCUrgentReferrals.SEVERE_ANEMIA.value)
                }
            }

            // 13. Fasting sugar ≥7mmol/L or Random sugar ≥11.1 mmol/L Or known DM/GDM patient not on treatment
            val fastingSugar = CommonUtils.getDoubleOrNull(maternalAssessment[RMNCH.ID_FASTING_BLOOD_SUGAR])?.takeIf { it > 0 }
            val randomSugar = CommonUtils.getDoubleOrNull(maternalAssessment[RMNCH.ID_RANDOM_BLOOD_SUGAR])?.takeIf { it > 0 }
            val dmPatient = isSelectionPresent(maternalAssessment[RMNCH.ID_DM_PATIENT], DefinedParams.Yes)
            val gdmPatient = isSelectionPresent(maternalAssessment[RMNCH.ID_GDM_PATIENT], DefinedParams.Yes)
            val onTreatment = isSelectionPresent(maternalAssessment[RMNCH.ID_ON_TREATMENT_DM_GDM], DefinedParams.Yes)

            if ((fastingSugar != null && fastingSugar >= 7.0) ||
                (randomSugar != null && randomSugar >= 11.1) ||
                ((dmPatient || gdmPatient) && !onTreatment)
            ) {
                urgentReferral.add(PNCUrgentReferrals.SUGAR_DM_GDM_NOT_ON_TREATMENT.value)
            }

            // 14. Urinary Bilirubin present
            if (isSelectionPresent(maternalAssessment[RMNCH.ID_URINARY_BILIRUBIN])) {
                urgentReferral.add(PNCUrgentReferrals.URINARY_BILIRUBIN_PRESENT.value)
            }
        }

        return urgentReferral
    }

    /**
     * Identifies non-urgent referral conditions from the maternal health assessment data.
     * Includes checks for moderate anemia, breast issues, mild fever, and monitoring
     * of patients already on treatment for chronic conditions.
     *
     * @param resultMap The assessment result map containing maternal health data.
     * @return A list of strings representing the non-urgent referral reasons found.
     */
    fun getNonUrgentReferral(resultMap: Map<String, Any>): List<String> {
        val nonUrgentReferral = arrayListOf<String>()

        (resultMap[RMNCH.ID_MATERNAL_HEALTH_ASSESSMENT] as? Map<*, *>)?.let { maternalAssessment ->
            // 1.Hb 8–10 (moderate anemia)
            CommonUtils.getDoubleOrNull(maternalAssessment[RMNCH.ID_HEMOGLOBIN])?.takeIf { it > 0 }?.let { hb ->
                if (hb in 8.0..10.0) {
                    nonUrgentReferral.add(PNCNonUrgentReferral.HB_MODERATE_ANEMIA.value)
                }
            }

            // 2.Cracked nipples / painful / swollen breasts with or without fever
            (maternalAssessment[RMNCH.ID_POSTPARTUM_DANGER_SIGNS] as? List<*>)?.let { dangerSigns ->
                val selectedSigns = dangerSigns
                    .filterIsInstance<Map<String, Any>>()
                    .mapNotNull {
                        it[DefinedParams.Value] as? String
                    }.toSet()

                if (selectedSigns.contains(PostpartumDangerSigns.BREAST_PAIN_SWELLING_FEVER.value)) {
                    nonUrgentReferral.add(PNCNonUrgentReferral.BREAST_ISSUES.value)
                }
            }

            // 3.Fever - >=100°F
            CommonUtils.getDoubleOrNull(maternalAssessment[RMNCH.ID_TEMPERATURE])?.takeIf { it > 0 }?.let { temp ->
                if (temp >= 100.0) {
                    nonUrgentReferral.add(PNCNonUrgentReferral.FEVER.value)
                }
            }

            val isKnownHtn = isSelectionPresent(maternalAssessment[RMNCH.ID_KNOWN_HTN], DefinedParams.Yes)
            val isEclampsia = isSelectionPresent(maternalAssessment[RMNCH.ID_ECLAMPSIA], DefinedParams.Yes)
            val isOnTreatmentHtn = isSelectionPresent(maternalAssessment[RMNCH.ID_ON_TREATMENT_HTN_ECLAMPSIA], DefinedParams.Yes)

            // 4. Known HTN patient or PW with eclampsia on treatment
            if ((isKnownHtn || isEclampsia) && isOnTreatmentHtn) {
                nonUrgentReferral.add(PNCNonUrgentReferral.HTN_ECLAMPSIA_ON_TREATMENT.value)
            }

            val dmPatient = isSelectionPresent(maternalAssessment[RMNCH.ID_DM_PATIENT], DefinedParams.Yes)
            val gdmPatient = isSelectionPresent(maternalAssessment[RMNCH.ID_GDM_PATIENT], DefinedParams.Yes)
            val onTreatmentDmGdm = isSelectionPresent(maternalAssessment[RMNCH.ID_ON_TREATMENT_DM_GDM], DefinedParams.Yes)

            // 5.Known DM or GDM patient on treatment
            if ((dmPatient || gdmPatient) && onTreatmentDmGdm) {
                nonUrgentReferral.add(PNCNonUrgentReferral.DM_GDM_ON_TREATMENT.value)
            }
        }

        return nonUrgentReferral
    }

    /**
     * Identifies care gaps in supplementation and contraception for the PNC period.
     * Supplementation gaps (Vitamin A, IFA, Calcium) are grouped into a single reason.
     *
     * @param resultMap The assessment result map containing maternal health and contraception data.
     * @return A list of strings representing the care gaps found.
     */
    fun getPncGaps(resultMap: Map<String, Any>): List<String> {
        val pncGaps = arrayListOf<String>()

        (resultMap[RMNCH.ID_MATERNAL_HEALTH_ASSESSMENT] as? Map<*, *>)?.let { maternalAssessment ->
            val supplementationGaps = arrayListOf<String>()

            // 1. Supplementation (Vitamin A, IFA, Calcium)
            // Vitamin A
            if (!isSelectionPresent(maternalAssessment[RMNCH.ID_VITAMIN_A_CONSUMED], DefinedParams.Yes)) {
                supplementationGaps.add(PNCSupplementation.VITAMIN_A.value)
            }

            // IFA and Calcium
            val daysSinceDelivery = CommonUtils.getInteger(resultMap[RMNCH.ID_DAYS_SINCE_DELIVERY])
            val expectedTablets = daysSinceDelivery + 1

            // IFA (Non-optional integer)
            val ifaConsumed = CommonUtils.getInteger(maternalAssessment[RMNCH.ID_IFA_TABLETS_CONSUMED])
            if (ifaConsumed < expectedTablets) {
                supplementationGaps.add(PNCSupplementation.IFA.value)
            }

            // Calcium (Non-optional integer)
            val calciumConsumed = CommonUtils.getInteger(maternalAssessment[RMNCH.ID_CALCIUM_TABLETS_CONSUMED])
            if (calciumConsumed < expectedTablets) {
                supplementationGaps.add(PNCSupplementation.CALCIUM.value)
            }

            if (supplementationGaps.isNotEmpty()) {
                pncGaps.add("${PNCGaps.SUPPLEMENTATION.value} (${supplementationGaps.joinToString(", ")})")
            }
        }

        // 2. Not using postpartum contraception
        (resultMap[RMNCH.ID_POSTPARTUM_CONTRACEPTION] as? Map<*, *>)?.let { contraception ->
            if (isSelectionPresent(contraception[RMNCH.ID_FAMILY_PLANNING_METHODS], DefinedParams.None)) {
                pncGaps.add(PNCGaps.CONTRACEPTION_GAP.value)
            }
        }

        return pncGaps
    }

    /**
     * Checks if blood pressure exceeds defined limits for given systolic and diastolic values.
     *
     * @param systolic The systolic pressure value.
     * @param diastolic The diastolic pressure value.
     * @return True if either value exceeds its respective limit, false otherwise.
     */
    private fun isBpHigh(
        systolic: Int,
        diastolic: Int,
    ): Boolean {
        if (systolic <= 0 && diastolic <= 0) return false
        return systolic >= AssessmentDefinedParams.BP_SYSTOLIC_THRESHOLD.toInt() ||
            diastolic >= AssessmentDefinedParams.BP_DIASTOLIC_THRESHOLD.toInt()
    }

    /**
     * Checks if a specific selection or value is present for a key in the assessment data.
     *
     * @param value The value to check.
     * @param target The expected target value (defaults to "present").
     * @return True if the value matches the target (case-insensitive), false otherwise.
     */
    private fun isSelectionPresent(
        value: Any?,
        target: String = "present",
    ): Boolean = (value as? String)?.equals(target, ignoreCase = true) == true
}
