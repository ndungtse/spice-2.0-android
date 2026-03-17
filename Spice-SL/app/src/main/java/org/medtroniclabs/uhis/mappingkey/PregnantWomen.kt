package org.medtroniclabs.uhis.mappingkey

import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DefinedParams

/**
 * Contains constants and some helper methods for pregnant women registration
 */
object PregnantWomen {
    /**
     * Card : Pregnancy Details & History
     */
    const val ID_PREGNANCY_DETAILS_AND_HISTORY = "pregnancyDetailsAndHistory"

    /**
     * Date : LMP (Last Menstrual Period)
     */
    const val ID_LMP = "lmp"

    const val ID_TOO_EARLY_TITLE = "TooEarlyTitle"
    const val ID_TOO_EARLY_DESC1 = "TooEarlyDesc1"
    const val ID_TOO_EARLY_DESC2 = "TooEarlyDesc2"

    /**
     * Edit : 👶 EDD (Expected Date of Delivery)
     */
    const val ID_EDD_TITLE = "EDDTitle"
    const val ID_EDD = "EDD"

    /**
     * Edit : 📅 Gestational Week
     */
    const val ID_GESTATIONAL_WEEK_TITLE = "gestationalWeekTitle"
    const val ID_GESTATIONAL_WEEK = "gestationalWeek"

    /**
     * Spinner : Pregnancy Test
     */
    const val ID_PREGNANCY_TEST = "pregnancyTest"

    /**
     * Edit : Gravida
     */
    const val ID_GRAVIDA = "gravida"

    /**
     * Edit : Parity (Number of Total Births)
     */
    const val ID_PARITY = "parity"

    /**
     * Edit : Number of Living Children
     */
    const val ID_LIVING_CHILDREN = "livingChildren"

    /**
     * Age : Age of last child
     */
    const val ID_AGE_OF_LAST_CHILD = "ageOfLastChild"

    /**
     * Card : Health & Risk Screening
     */
    const val ID_HEALTH_RISK_SCREENING = "healthRiskScreening"

    /**
     * DialogCheckbox : Obstetric complications
     */
    const val ID_OBSTETRIC_COMPLICATIONS = "obstetricComplications"

    /**
     * DialogCheckbox : Medical complications
     */
    const val ID_MEDICAL_COMPLICATIONS = "medicalComplications"

    /**
     * DialogCheckbox : Current medical conditions
     */
    const val ID_CURRENT_MEDICAL_CONDITIONS = "currentMedicalConditions"

    const val SSP_18 = 18

    /**
     * Number days to consider whether to continue pregnancy registration or not
     */
    const val LMP_THRESHOLD_DAYS = 42

    /**
     * Options of H/O miscarriage and H/O Induced abortion to be disabled if the gap between parity and gravida is 1.
     * These  options should appear only if the gap is 2 or more indicating chances of abortion
     */
    val OBSTETRIC_COMPLICATIONS_TO_IGNORE = listOf("hoMiscarriage", "hoInducedAbortion")

    const val GRAVIDA_PARITY_IGNORE_DIFF = 2

    /**
     * Calculates risk factors based on given hashmap input
     *
     * like age related risks, short term births, multi para
     * and medical conditions and complications
     */
    fun computeRiskFactors(input: Map<String, Any?>): List<String> {
        val riskFactors = arrayListOf<String>()
        val lastMenstrualDateString = input[ID_LMP] as String
        val dateOfBirth = input[MemberRegistration.dateOfBirth] as String
        val calculatedAge = DateUtils.calculateAgeToDate(dateOfBirth, lastMenstrualDateString)
        // Age related risk factors
        if (calculatedAge < 18) {
            riskFactors.add("Age <18 years")
        } else if (calculatedAge > 35) {
            riskFactors.add("Age >35 years")
        }
        val livingChildren = (input[ID_LIVING_CHILDREN] as? Double) ?: 0.0
        if (livingChildren >= 1) {
            val ageOfLastChild = input[ID_AGE_OF_LAST_CHILD] as String
            val calculatedAgeOfLastChild = DateUtils.calculateAge(ageOfLastChild)
            // Short term birth
            if (calculatedAgeOfLastChild < 2) {
                riskFactors.add("Short birth spacing <2 year")
            }
        }
        val parity = (input[ID_PARITY] as? Double) ?: 0.0
        // Multi para
        if (parity > 3) {
            riskFactors.add("Multipara >3")
        }
        val obstetricComplications = mutableListOf<String>()
        (input[ID_OBSTETRIC_COMPLICATIONS] as? List<Map<String, Any>>)?.let {
            it.forEach { complicationMap ->
                obstetricComplications.add(complicationMap[DefinedParams.Value] as String)
            }
        } ?: (input[ID_OBSTETRIC_COMPLICATIONS] as? List<String>)?.let {
            obstetricComplications.addAll(it)
        }
        val medicalComplications = mutableListOf<String>()
        (input[ID_MEDICAL_COMPLICATIONS] as? List<Map<String, Any>>)?.let {
            it.forEach { complicationMap ->
                medicalComplications.add(complicationMap[DefinedParams.Value] as String)
            }
        } ?: (input[ID_MEDICAL_COMPLICATIONS] as? List<String>)?.let {
            medicalComplications.addAll(it)
        }
        val medicalConditions = mutableListOf<String>()
        (input[ID_CURRENT_MEDICAL_CONDITIONS] as? List<Map<String, Any>>)?.let {
            it.forEach { conditionsMap ->
                medicalConditions.add(conditionsMap[DefinedParams.Value] as String)
            }
        } ?: (input[ID_CURRENT_MEDICAL_CONDITIONS] as? List<String>)?.let {
            medicalConditions.addAll(it)
        }
        if (obstetricComplications.contains("previousCSection")) {
            riskFactors.add("Previous C-section")
        }
        if (obstetricComplications.contains("previousAssistedDelivery")) {
            riskFactors.add("Previous Assisted delivery")
        }
        if (obstetricComplications.contains("hoStillBirth")) {
            riskFactors.add("H/O still birth")
        }
        if (obstetricComplications.contains("hoMiscarriage") ||
            obstetricComplications.contains("hoInducedAbortion")
        ) {
            riskFactors.add("H/O miscarriage & abortion")
        }
        if (obstetricComplications.contains("previousPretermLabour")) {
            riskFactors.add("Previous Preterm Labour")
        }
        if (medicalComplications.contains("excessiveBleedingDuringAfterDelivery")) {
            riskFactors.add("H/O excessive bleeding during/after delivery")
        }
        if (medicalComplications.contains("bleedingAfter24Weeks2ndTrimester")) {
            riskFactors.add("H/O Bleeding after 2nd trimester")
        }
        if (medicalComplications.contains("preEclampsia") ||
            medicalComplications.contains("eclampsia")
        ) {
            riskFactors.add("H/O Pre-eclampsia/Eclampsia")
        }
        if (medicalComplications.contains("gestationalDiabetes")) {
            riskFactors.add("H/O GDM")
        }
        if (medicalComplications.contains("infectionSepsis")) {
            riskFactors.add("H/O Infection/Sepsis during pregnancy")
        }
        if (medicalComplications.contains("severeAnemia")) {
            riskFactors.add("H/O Severe Anemia")
        }
        if (medicalConditions.contains("highBloodPressure")) {
            riskFactors.add("Known patient of HTN")
        }
        if (medicalConditions.contains("diabetes")) {
            riskFactors.add("Known patient of DM")
        }
        if (medicalConditions.contains("heartDisease")) {
            riskFactors.add("Known patient of Heart Disease")
        }
        if (medicalConditions.contains("tuberculosis")) {
            riskFactors.add("Existing patient of Tuberculosis")
        }
        if (medicalConditions.contains("asthma")) {
            riskFactors.add("Known patient of Asthma")
        }
        if (medicalConditions.contains("thyroidDisease")) {
            riskFactors.add("Known patient of Thyroid disease")
        }
        if (medicalConditions.contains("epilepsy")) {
            riskFactors.add("Known patient of Epilepsy")
        }
        if (medicalConditions.contains("kidneyDisease")) {
            riskFactors.add("Known patient of Kidney Disease")
        }
        if (medicalConditions.contains("liverDisease")) {
            riskFactors.add("Known patient of Liver Disease")
        }
        if (medicalConditions.contains("autoimmuneDisease")) {
            riskFactors.add("Known patient of Autoimmune Disease")
        }
        return riskFactors
    }
}
