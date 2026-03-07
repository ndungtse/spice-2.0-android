package com.medtroniclabs.spice.ui.assessment.rmnch

import android.content.Context
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.formgeneration.config.ViewType
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentCommonUtils
import java.text.DecimalFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object RMNCH {
    const val RMNCHChildHoodVisit = "RMNCHChildHoodVisit"
    const val PlaceOfDelivery = "placeOfDelivery"
    const val ANC = "anc"
    const val PNC = "pncMother"
    const val PNCNeonatal = "pncNeonatal"
    const val ChildHoodVisit = "pncChild"
    const val lastMenstrualPeriod = "lastMenstrualPeriod"
    const val estimatedDeliveryDate = "estimatedDeliveryDate"
    const val visitNo = "visitNo"
    const val PNC_MENU = "pnc"
    const val ANC_MENU = "anc"
    const val CHILD_MENU = "ChildHood_Visit"
    const val DateOfDelivery = "dateOfDelivery"
    const val NoOfNeonate = "noOfNeonates"
    const val gestationalAge = "gestationalAge"
    const val Miscarriage = "miscarriage"
    const val DeathOfMother = "deathOfMother"
    const val childhoodVisitSigns = "childhoodVisitSigns"
    const val childhoodVisitSignsLabel = "Childhood Visit Signs"
    const val pncChildSigns = "pncChildSigns"
    const val otherChildhoodVisitSigns = "otherChildhoodVisitSigns"
    const val otherSigns = "otherSigns"
    const val ancSigns = "ancSigns"
    const val ancSignsLabel = "ANC Signs"
    const val otherAncSigns = "otherAncSigns"
    const val pncNeonateSigns = "pncNeonateSigns"
    const val pncNeonateSignsLabel = "PNC Neonate Signs"
    const val pncNeonatalSigns = "pncNeonatalSigns"
    const val otherPncNeonateSigns = "otherPncNeonateSigns"
    const val pncMotherSigns = "pncMotherSigns"
    const val NeonatePatientId = "neonatePatientId"
    const val NeonateOutcome = "neonateOutcome"
    const val NeonatePatientReferenceId = "neonatePatientReferenceId"
    const val pncMotherSignsLabel = "PNC Mother Signs"
    const val otherPncMotherSigns = "otherPncMotherSigns"
    const val PREGNANCY_MIN_AGE = 10
    const val PREGNANCY_MAX_AGE = 49
    const val PNCMOTHER = "PNC-Mother"
    const val PNCNEONATE = "PNC-Baby"
    const val pnc_mother_key = "PNC_MOTHER"
    const val pnc_neonate_key = "PNC_NEONATE"
    const val muac = "muac"
    const val Pregnancy = "Pregnant, "

    const val ANCVisitNo = "ANC Visit "
    const val PNCVisitNo = "PNC Visit "
    const val ChildHoodVisitNo = "Childhood Visit "

    const val deathOfNewborn = "deathOfNewborn"
    const val newbornReferredToSBCU = "newbornReferredToSBCU"
    const val lowBirthWeight = "lowBirthWeight"
    const val deathOfBaby = "deathOfBaby"
    const val isDeceased = "isDeceased"
    const val deceasedReason = "deceasedReason"

    const val otherPlaceOfDelivery = "otherPlaceOfDelivery"

    const val childHoodVisitMaxMonth = 15

    /**
     * CardView : Pregnancy History
     */
    const val ID_PREGNANCY_HISTORY = "pregnancyHistory"

    /**
     * CardView : Maternal Health Assessment
     */
    const val ID_MATERNAL_HEALTH_ASSESSMENT = "maternalHealthAssessment"

    /**
     * CardView : Postpartum Contraception
     */
    const val ID_POSTPARTUM_CONTRACEPTION = "postpartumContraception"

    /**
     * Spinner : Modern Family planning method being used
     */
    const val ID_FAMILY_PLANNING_METHODS = "familyPlanningMethods"

    /**
     * DialogCheckbox : Danger Signs present
     */
    const val ID_POSTPARTUM_DANGER_SIGNS = "postpartumDangerSigns"

    /**
     * EditText : Hemoglobin (Hb)
     */
    const val ID_HEMOGLOBIN = "hemoglobin"

    /**
     * EditText : Temperature
     */
    const val ID_TEMPERATURE = "temperature"

    /**
     * EditText : Pulse
     */
    const val ID_PULSE = "pulse"

    /**
     * EditText : Systolic
     */
    const val ID_SYSTOLIC = "systolic"

    /**
     * EditText : Diastolic
     */
    const val ID_DIASTOLIC = "diastolic"

    /**
     * SingleSelectionView : Edema
     */
    const val ID_EDEMA = "edema"

    /**
     * SingleSelectionView : Urinary Albumin
     */
    const val ID_URINARY_ALBUMIN = "urinaryAlbumin"

    /**
     * SingleSelectionView : Urinary Bilirubin
     */
    const val ID_URINARY_BILIRUBIN = "urinaryBilirubin"

    /**
     * RadioGroup : Known DM patient
     */
    const val ID_DM_PATIENT = "dmPatient"

    /**
     * RadioGroup : Known GDM patient
     */
    const val ID_GDM_PATIENT = "gdmPatient"

    /**
     * SingleSelectionView : Blood Sugar
     */
    const val ID_BLOOD_SUGAR = "bloodSugar"

    /**
     * EditText : Fasting (Blood Sugar)
     */
    const val ID_FASTING_BLOOD_SUGAR = "fastingBloodSugar"

    /**
     * EditText : Random (Blood Sugar)
     */
    const val ID_RANDOM_BLOOD_SUGAR = "randomBloodSugar"

    /**
     * RadioGroup : On treatment for DM/GDM
     */
    const val ID_ON_TREATMENT_DM_GDM = "onTreatmentDmGdm"

    /**
     * RadioGroup : Known HTN patient
     */
    const val ID_KNOWN_HTN = "htnPatient"

    /**
     * RadioGroup : Known Eclampsia patient
     */
    const val ID_ECLAMPSIA = "eclampsia"

    /**
     * RadioGroup : On treatment for HTN/ Pre-eclampsia/ Eclampsia
     */
    const val ID_ON_TREATMENT_HTN_ECLAMPSIA = "onTreatmentHtnEclampsia"

    /**
     * Value : Days since delivery
     */
    const val ID_DAYS_SINCE_DELIVERY = "daysSinceDelivery"

    /**
     * RadioGroup : Vitamin A capsule consumed
     */
    const val ID_VITAMIN_A_CONSUMED = "vitaminAConsumed"

    /**
     * EditText : IFA Tablets Consumed
     */
    const val ID_IFA_TABLETS_CONSUMED = "ifaTabletsConsumed"

    /**
     * EditText : Calcium Tablets Consumed
     */
    const val ID_CALCIUM_TABLETS_CONSUMED = "calciumTabletsConsumed"

    /**
     * Instructions : Maternal danger signs
     */
    const val ID_MATERNAL_DANGER_SIGNS = "maternalDangerSigns"

    /**
     * Instructions : Maternal nutrition
     */
    const val ID_MATERNAL_NUTRITION = "maternalNutrition"

    /**
     * Instructions : Maternal nutrition
     */
    const val ID_FAMILY_PLANNING = "familyPlanning"

    /**
     * TextLabel : Care of the Mother
     */
    const val ID_COUNSELLING_MOTHER_CARE = "counsellingMotherCare"

    fun getValueFromMap(
        resultMap: HashMap<String, Any>,
        id: String,
        viewType: String,
        workflowName: String?,
        isBooleanAnswer: Boolean,
        triple: Triple<String, String, String>,
        context: Context,
    ): String {
        if (resultMap.containsKey(workflowName)) {
            val actualMap = resultMap[workflowName]
            if (actualMap is Map<*, *>) {
                val value = actualMap[id]
                if (viewType == ViewType.VIEW_TYPE_FORM_DATEPICKER && value is String) {
                    return DateUtils.convertDateFormat(
                        value,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DateUtils.DATE_ddMMyyyy,
                    )
                } else if (viewType == ViewType.VIEW_TYPE_DIALOG_CHECKBOX) {
                    return getDangerSignValue(value, triple.third, actualMap)
                } else {
                    when (value) {
                        is String -> {
                            return if (id == muac) {
                                context.getString(
                                    R.string.nutrition_summary,
                                    value,
                                    AssessmentCommonUtils.getNutritionStatus(
                                        value,
                                        context,
                                    ),
                                )
                            } else if (id == PlaceOfDelivery) {
                                return if (actualMap.containsKey(otherPlaceOfDelivery)) {
                                    ("$value - ${actualMap[otherPlaceOfDelivery]}")
                                } else {
                                    value
                                }
                            } else {
                                value
                            }
                        }

                        is Boolean -> {
                            return if (isBooleanAnswer) {
                                if (value) triple.first else triple.second
                            } else {
                                value.toString()
                            }
                        }

                        is Double -> {
                            val df = DecimalFormat("#.#")
                            return if (id.equals(gestationalAge, true)) {
                                "${df.format(value)} ${getWeekPeriod(value, context)}"
                            } else {
                                df.format(value)
                            }
                        }
                    }
                }
            }
        }
        return triple.third
    }

    private fun getWeekPeriod(
        gestationWeek: Double,
        context: Context,
    ): String =
        if (gestationWeek == 1.0) {
            context.getString(R.string.week)
        } else {
            context.getString(R.string.weeks)
        }

    private fun getDangerSignValue(
        value: Any?,
        hyphenSymbol: String,
        actualMap: Map<*, *>,
    ): String {
        val result = ArrayList<String>()
        if (value is List<*>) {
            value.forEach {
                if (it is Map<*, *>) {
                    val key = it[DefinedParams.NAME]
                    if (key is String) {
                        if (key.equals(DefinedParams.Other, true)) {
                            if (actualMap.containsKey(otherAncSigns)) {
                                val otherSignValue = actualMap[otherAncSigns]
                                result.add("$key - $otherSignValue")
                            } else if (actualMap.containsKey(otherChildhoodVisitSigns)) {
                                val otherSignValue = actualMap[otherChildhoodVisitSigns]
                                result.add("$key - $otherSignValue")
                            } else if (actualMap.containsKey(otherPncMotherSigns)) {
                                val otherSignValue = actualMap[otherPncMotherSigns]
                                result.add("$key - $otherSignValue")
                            } else if (actualMap.containsKey(otherPncNeonateSigns)) {
                                val otherSignValue = actualMap[otherPncNeonateSigns]
                                result.add("$key - $otherSignValue")
                            } else {
                                result.add(key)
                            }
                        } else {
                            result.add(key)
                        }
                    }
                }
            }
            return result.joinToString(", ")
        }
        return hyphenSymbol
    }

    private fun calculatePregnancyMonth(lmp: Date): Double {
        val today = Date()
        val diffInMillis = abs(today.time - lmp.time)
        val diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
        val weeks = diff / 7.0
        return (weeks / 4.0)
    }

    fun calculateNextANCVisitDate(
        lmp: Date,
        isMedicalReview: Boolean = false,
    ): Date? {
        return when (calculatePregnancyMonth(lmp)) {
            in 0.0..4.0 -> {
                if (isMedicalReview) {
                    DateUtils.addDaysToDate(lmp, ((28 * 5)))
                } else {
                    DateUtils.addDaysToDate(lmp, ((28 * 5) - 15))
                }
            }

            in 4.1..5.0 -> {
                if (isMedicalReview) {
                    DateUtils.addDaysToDate(lmp, ((28 * 6)))
                } else {
                    DateUtils.addDaysToDate(lmp, ((28 * 6) - 15))
                }
            }

            in 5.1..6.0 -> {
                if (isMedicalReview) {
                    DateUtils.addDaysToDate(lmp, ((28 * 7)))
                } else {
                    DateUtils.addDaysToDate(lmp, ((28 * 7) - 15))
                }
            }

            in 6.1..7.0 -> {
                if (isMedicalReview) {
                    DateUtils.addDaysToDate(lmp, ((28 * 8)))
                } else {
                    DateUtils.addDaysToDate(lmp, ((28 * 8) - 15))
                }
            }

            in 7.1..8.9 -> {
                if (isMedicalReview) {
                    DateUtils.addDaysToDate(lmp, ((28 * 9)))
                } else {
                    DateUtils.addDaysToDate(lmp, ((28 * 9) - 15))
                }
            }

            else -> {
                return null
            }
        }
    }

    fun calculateNextChildHoodVisitDate(
        age: Int,
        birthDate: Date,
    ): Date? {
        return when (age) {
            in 0..4 -> {
                DateUtils.addMonthsToDate(birthDate, 5)
            }

            in 4..5 -> {
                DateUtils.addMonthsToDate(birthDate, 9)
            }

            in 6..9 -> {
                DateUtils.addMonthsToDate(birthDate, 12)
            }

            in 10..12 -> {
                DateUtils.addMonthsToDate(birthDate, 15)
            }

            else -> {
                return null
            }
        }
    }

    fun calculateNextPNCVisitDate(deliveryDate: Date): Date? {
        when (DateUtils.daysBetweenDates(deliveryDate, Date())) {
            in 0..1 -> {
                return DateUtils.addDaysToDate(deliveryDate, 3)
            }

            in 2..3 -> {
                return DateUtils.addDaysToDate(deliveryDate, 7)
            }

            in 4..7 -> {
                return DateUtils.addDaysToDate(deliveryDate, 15)
            }

            else -> {
                return null
            }
        }
    }

    fun getMenuName(workflowName: String?): String {
        when (workflowName) {
            ANC -> return ANC_MENU.uppercase(Locale.getDefault())
            ChildHoodVisit -> return CHILD_MENU.uppercase(Locale.getDefault())
            PNC -> return PNC_MENU.uppercase(Locale.getDefault())
        }
        return MenuConstants.RMNCH_MENU_ID.uppercase(Locale.getDefault())
    }

    fun getDeathStatus(
        map: HashMap<String, Any>,
        workFlowName: String,
        keyName: String,
    ): Boolean {
        var deathOfMother = false
        val workFlowMap = map[workFlowName]
        if (workFlowMap is Map<*, *> && workFlowMap.containsKey(keyName)) {
            if (workFlowMap[keyName] is Boolean) {
                deathOfMother = workFlowMap[keyName] as Boolean
            }
        }
        return deathOfMother
    }
}
