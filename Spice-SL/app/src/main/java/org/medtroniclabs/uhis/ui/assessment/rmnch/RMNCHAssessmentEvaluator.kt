package org.medtroniclabs.uhis.ui.assessment.rmnch

import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DefinedParams
import org.medtroniclabs.uhis.db.entity.PregnancyDetail
import org.medtroniclabs.uhis.ui.assessment.AssessmentDefinedParams

/**
 * Utility class for evaluating RMNCH assessment conditions
 * Provides reusable helper functions for both AssessmentRMNCHFragment and AssessmentRMNCHSummaryFragment
 */
object RMNCHAssessmentEvaluator {
    /**
     * Helper method to get value from nested structure
     * Checks all ANC form groups first, then top level
     */
    private fun getValueFromNestedMap(
        resultMap: HashMap<String, Any>,
        key: String,
    ): Any? {
        // Check all ANC form groups for the field
        for (groupId in AssessmentDefinedParams.ANC_FORM_GROUPS) {
            val groupMap = resultMap[groupId] as? Map<*, *>
            groupMap?.get(key)?.let { return it }
        }

        // If not found in any group, check top level
        return resultMap[key]
    }

    /**
     * Filters out "none" option from illness list
     * Used to exclude "none" from conditional checks and treatment dialog
     * @param illnessList List of illness items (can be ArrayList<Map<*, *>> or ArrayList<HashMap<String, Any>>)
     * @return Filtered list excluding "none" entries
     */
    private fun filterOutNoneOption(illnessList: ArrayList<*>?): ArrayList<*> {
        if (illnessList == null || illnessList.isEmpty()) {
            @Suppress("UNCHECKED_CAST")
            return arrayListOf<Any>() as ArrayList<*>
        }

        @Suppress("UNCHECKED_CAST")
        return illnessList.filter { illness ->
            val illnessMap = illness as? Map<*, *> ?: return@filter true
            val id = illnessMap[DefinedParams.ID]?.toString()?.lowercase() ?: ""
            val name = illnessMap[DefinedParams.NAME]?.toString()?.lowercase() ?: ""

            // Filter out if id is "none" or name is "None"
            id != AssessmentDefinedParams.ILLNESS_NONE_ID &&
                name != DefinedParams.None.lowercase()
        } as ArrayList<*>
    }

    /**
     * Helper method to check if existing HTN illness is present
     */
    fun hasExistingHTNIllness(resultMap: HashMap<String, Any>): Boolean {
        val existingIllness = getValueFromNestedMap(resultMap, AssessmentDefinedParams.PREGNANT_WOMAN_EXISTING_ILLNESS) as? ArrayList<*> ?: return false
        val filteredIllness = filterOutNoneOption(existingIllness)
        return filteredIllness.any { illness ->
            val illnessMap = illness as? Map<*, *> ?: return@any false
            val id = illnessMap[DefinedParams.ID]?.toString() ?: ""
            val name = illnessMap[DefinedParams.name]?.toString() ?: ""
            id.equals(AssessmentDefinedParams.ILLNESS_HTN, ignoreCase = true) ||
                name.equals(AssessmentDefinedParams.ILLNESS_HTN, ignoreCase = true) ||
                id.contains(AssessmentDefinedParams.ILLNESS_HYPERTENSION, ignoreCase = true) ||
                name.contains(AssessmentDefinedParams.ILLNESS_HYPERTENSION, ignoreCase = true)
        }
    }

    /**
     * Helper method to check if existing DM illness is present
     */
    fun hasExistingDMIllness(resultMap: HashMap<String, Any>): Boolean {
        val existingIllness = getValueFromNestedMap(resultMap, AssessmentDefinedParams.PREGNANT_WOMAN_EXISTING_ILLNESS) as? ArrayList<*> ?: return false
        val filteredIllness = filterOutNoneOption(existingIllness)
        return filteredIllness.any { illness ->
            val illnessMap = illness as? Map<*, *> ?: return@any false
            val id = illnessMap[DefinedParams.ID]?.toString() ?: ""
            val name = illnessMap[DefinedParams.name]?.toString() ?: ""
            id.equals(AssessmentDefinedParams.ILLNESS_DM, ignoreCase = true) ||
                name.equals(AssessmentDefinedParams.ILLNESS_DM, ignoreCase = true) ||
                id.contains(AssessmentDefinedParams.ILLNESS_DIABETES, ignoreCase = true) ||
                name.contains(AssessmentDefinedParams.ILLNESS_DIABETES, ignoreCase = true)
        }
    }

    /**
     * Generic helper method to check if a specific chronic illness exists
     */
    fun hasChronicIllness(
        resultMap: HashMap<String, Any>,
        illnessName: String,
    ): Boolean {
        val existingIllness = getValueFromNestedMap(resultMap, AssessmentDefinedParams.PREGNANT_WOMAN_EXISTING_ILLNESS) as? ArrayList<*> ?: return false
        val filteredIllness = filterOutNoneOption(existingIllness)
        return filteredIllness.any { illness ->
            val illnessMap = illness as? Map<*, *> ?: return@any false
            val id = illnessMap[DefinedParams.ID]?.toString() ?: ""
            val name = illnessMap[DefinedParams.name]?.toString() ?: ""
            id.contains(illnessName, ignoreCase = true) || name.contains(illnessName, ignoreCase = true)
        }
    }

    /**
     * Check if a specific illness is in the treatment list
     */
    fun isIllnessOnTreatment(
        resultMap: HashMap<String, Any>,
        illnessName: String,
    ): Boolean {
        val treatmentList = getValueFromNestedMap(resultMap, AssessmentDefinedParams.PREGNANT_WOMAN_ON_TREATMENT) as? ArrayList<*> ?: return false
        return treatmentList.any { treatment ->
            val treatmentMap = treatment as? Map<*, *> ?: return@any false
            val id = treatmentMap[DefinedParams.ID]?.toString() ?: ""
            val name = treatmentMap[DefinedParams.name]?.toString() ?: ""
            id.contains(illnessName, ignoreCase = true) || name.contains(illnessName, ignoreCase = true)
        }
    }

    /**
     * Check if any chronic illness (Diabetes, Heart Disease, TB, Asthma, Thyroid, Kidney Disease) exists
     */
    fun hasAnyChronicIllness(resultMap: HashMap<String, Any>): Boolean =
        hasExistingDMIllness(resultMap) ||
            hasChronicIllness(resultMap, AssessmentDefinedParams.ILLNESS_HEART_DISEASE) ||
            hasChronicIllness(resultMap, AssessmentDefinedParams.ILLNESS_TUBERCULOSIS) ||
            hasChronicIllness(resultMap, AssessmentDefinedParams.ILLNESS_TB) ||
            hasChronicIllness(resultMap, AssessmentDefinedParams.ILLNESS_ASTHMA) ||
            hasChronicIllness(resultMap, AssessmentDefinedParams.ILLNESS_THYROID) ||
            hasChronicIllness(resultMap, AssessmentDefinedParams.ILLNESS_KIDNEY_DISEASE)

    /**
     * Check if chronic illness exists but is NOT on treatment
     */
    fun hasChronicIllnessNotOnTreatment(resultMap: HashMap<String, Any>): Boolean {
        if (!hasAnyChronicIllness(resultMap)) return false

        // Check each chronic illness - if any exists but is not in treatment, return true
        val chronicIllnesses = listOf(
            AssessmentDefinedParams.ILLNESS_DM,
            AssessmentDefinedParams.ILLNESS_DIABETES,
            AssessmentDefinedParams.ILLNESS_HEART_DISEASE,
            AssessmentDefinedParams.ILLNESS_TUBERCULOSIS,
            AssessmentDefinedParams.ILLNESS_TB,
            AssessmentDefinedParams.ILLNESS_ASTHMA,
            AssessmentDefinedParams.ILLNESS_THYROID,
            AssessmentDefinedParams.ILLNESS_KIDNEY_DISEASE,
        )

        return chronicIllnesses.any { illnessName ->
            hasChronicIllness(resultMap, illnessName) && !isIllnessOnTreatment(resultMap, illnessName)
        }
    }

    /**
     * Check if chronic illness exists AND is on treatment
     */
    fun hasChronicIllnessWithTreatment(resultMap: HashMap<String, Any>): Boolean {
        if (!hasAnyChronicIllness(resultMap)) return false

        // Check each chronic illness - if any exists and is in treatment, return true
        val chronicIllnesses = listOf(
            AssessmentDefinedParams.ILLNESS_DM,
            AssessmentDefinedParams.ILLNESS_DIABETES,
            AssessmentDefinedParams.ILLNESS_HEART_DISEASE,
            AssessmentDefinedParams.ILLNESS_TUBERCULOSIS,
            AssessmentDefinedParams.ILLNESS_TB,
            AssessmentDefinedParams.ILLNESS_ASTHMA,
            AssessmentDefinedParams.ILLNESS_THYROID,
            AssessmentDefinedParams.ILLNESS_KIDNEY_DISEASE,
        )

        return chronicIllnesses.any { illnessName ->
            hasChronicIllness(resultMap, illnessName) && isIllnessOnTreatment(resultMap, illnessName)
        }
    }

    /**
     * Calculate member age from date of birth
     */
    fun getMemberAge(dateOfBirth: String?): Int? = dateOfBirth?.let { DateUtils.calculateAge(it) }

    /**
     * Calculate birth spacing in years from age of last child
     */
    fun calculateBirthSpacing(ageOfLastChild: String?): Double? =
        ageOfLastChild?.let {
            try {
                DateUtils.calculateAge(it).toDouble()
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Check if high risk pregnancy condition is met
     * High risk if: short birth spacing <2 years OR Age <18 OR >35 OR Multipara>3
     */
    fun isHighRiskPregnancy(
        dateOfBirth: String?,
        pregnancyDetail: PregnancyDetail?,
    ): Boolean {
        // Check age
        val age = getMemberAge(dateOfBirth)
        val isAgeRisk = age != null && (age < AssessmentDefinedParams.AGE_MIN_THRESHOLD || age > AssessmentDefinedParams.AGE_MAX_THRESHOLD)

        // Check birth spacing
        val birthSpacing = calculateBirthSpacing(pregnancyDetail?.ageOfLastChild)
        val isBirthSpacingRisk = birthSpacing != null && birthSpacing < AssessmentDefinedParams.BIRTH_SPACING_THRESHOLD_YEARS

        // Check multipara - store in local variable to avoid smart cast issue
        val parity = pregnancyDetail?.parity
        val isMultiparaRisk = parity != null && parity > AssessmentDefinedParams.MULTIPARA_THRESHOLD

        return isAgeRisk || isBirthSpacingRisk || isMultiparaRisk
    }

    /**
     * Check if suspected diabetes condition is met
     * Suspected/Existing Case of Diabetes (urine sugar/blood sugar/ known patient)
     */
    fun isSuspectedDiabetes(resultMap: HashMap<String, Any>): Boolean {
        // Check if known DM patient
        if (hasExistingDMIllness(resultMap)) return true

        // Check urine sugar
        val urinarySugar = getValueFromNestedMap(resultMap, AssessmentDefinedParams.URINARY_SUGAR) as? String
        if (urinarySugar == AssessmentDefinedParams.VALUE_PRESENT) return true

        // Check blood sugar fasting
        val bloodSugarFasting = (getValueFromNestedMap(resultMap, AssessmentDefinedParams.BLOOD_SUGAR_FASTING) as? Number)?.toDouble()
        if (bloodSugarFasting != null && bloodSugarFasting >= AssessmentDefinedParams.BLOOD_SUGAR_FASTING_THRESHOLD) return true

        // Check blood sugar random
        val bloodSugarRandom = (getValueFromNestedMap(resultMap, AssessmentDefinedParams.BLOOD_SUGAR_RANDOM) as? Number)?.toDouble()
        if (bloodSugarRandom != null && bloodSugarRandom >= AssessmentDefinedParams.BLOOD_SUGAR_RANDOM_THRESHOLD) return true

        return false
    }

    /**
     * Check if suspected pre-eclampsia condition is met
     * Suspected Pre-eclampsia (Urine Albumin or Edema WITH BP≥140/90 or existing HTN patient even with normal values <140/90)/High BP
     */
    fun isSuspectedPreEclampsia(resultMap: HashMap<String, Any>): Boolean {
        // Get BP values
        val systolic = (getValueFromNestedMap(resultMap, AssessmentDefinedParams.SYSTOLIC) as? String)?.toDouble()
            ?: (getValueFromNestedMap(resultMap, AssessmentDefinedParams.SYSTOLIC) as? Number)?.toDouble() ?: 0.0
        val diastolic = (getValueFromNestedMap(resultMap, AssessmentDefinedParams.DIASTOLIC) as? String)?.toDouble()
            ?: (getValueFromNestedMap(resultMap, AssessmentDefinedParams.DIASTOLIC) as? Number)?.toDouble() ?: 0.0
        val isHighBP = systolic >= AssessmentDefinedParams.BP_SYSTOLIC_THRESHOLD || diastolic >= AssessmentDefinedParams.BP_DIASTOLIC_THRESHOLD

        // Check existing HTN
        val hasHTN = hasExistingHTNIllness(resultMap)
        val isNormalBP = !isHighBP

        // Check urinary albumin
        val urinaryAlbumin = getValueFromNestedMap(resultMap, AssessmentDefinedParams.URINARY_ALBUMIN) as? String

        // Check edema
        val edema = getValueFromNestedMap(resultMap, AssessmentDefinedParams.EDEMA) as? String

        // Condition: (Urine Albumin OR Edema) WITH (BP≥140/90 OR existing HTN with normal BP)
        val hasUrineAlbuminOrEdema = urinaryAlbumin == AssessmentDefinedParams.VALUE_PRESENT || edema == AssessmentDefinedParams.VALUE_PRESENT
        val hasHighBPOrHTN = isHighBP || (hasHTN && isNormalBP)

        return hasUrineAlbuminOrEdema && hasHighBPOrHTN
    }

    /**
     * Check if pregnancy-related medical complications exist
     * Checks for: H/O Convulsions, H/O Postpartum hemorrhage, H/O Severe Anemia, H/O GDM
     */
    fun hasPregnancyRelatedMedicalComplications(resultMap: HashMap<String, Any>): Boolean {
        val previousComplications = getValueFromNestedMap(resultMap, AssessmentDefinedParams.PREVIOUS_PREGNANCY_COMPLICATIONS) ?: return false

        // Handle list of maps (with id/name) or list of strings (IDs)
        val complicationsList = when (previousComplications) {
            is ArrayList<*> -> previousComplications
            is List<*> -> previousComplications
            else -> return false
        }

        // IDs to check for: convulsions, postpartum_hemorrhage, severe_anemia, gestational_diabetes
        val targetComplicationIds = listOf(
            AssessmentDefinedParams.COMPLICATION_CONVULSIONS,
            AssessmentDefinedParams.COMPLICATION_POSTPARTUM_HEMORRHAGE,
            AssessmentDefinedParams.COMPLICATION_SEVERE_ANEMIA,
            AssessmentDefinedParams.COMPLICATION_GESTATIONAL_DIABETES,
        )

        return complicationsList.any { complication ->
            when (complication) {
                is Map<*, *> -> {
                    // Handle map structure with id/name
                    val id = complication[DefinedParams.ID]?.toString() ?: ""
                    val name = complication[DefinedParams.name]?.toString() ?: ""
                    targetComplicationIds.any { targetId ->
                        id.equals(targetId, ignoreCase = true) || name.contains(targetId, ignoreCase = true)
                    }
                }
                is String -> {
                    // Handle string ID directly
                    targetComplicationIds.any { targetId ->
                        complication.equals(targetId, ignoreCase = true)
                    }
                }
                else -> false
            }
        }
    }
}
