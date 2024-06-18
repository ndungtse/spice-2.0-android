package com.medtroniclabs.spice.ui.assessment.rmnch

import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.formgeneration.config.ViewType
import com.medtroniclabs.spice.ui.MenuConstants
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
    const val childhoodVisitSigns = "childhoodVisitSigns"
    const val pncChildSigns = "pncChildSigns"
    const val otherChildhoodVisitSigns = "otherChildhoodVisitSigns"
    const val otherSigns = "otherSigns"
    const val ancSigns = "ancSigns"
    const val otherAncSigns = "otherAncSigns"
    const val pncNeonateSigns = "pncNeonateSigns"
    const val pncNeonatalSigns = "pncNeonatalSigns"
    const val otherPncNeonateSigns = "otherPncNeonateSigns"
    const val pncMotherSigns = "pncMotherSigns"
    const val otherPncMotherSigns = "otherPncMotherSigns"
    const val PREGNANCY_MIN_AGE = 18
    const val PREGNANCY_MAX_AGE = 49


    fun getValueFromMap(
        resultMap: HashMap<String, Any>,
        id: String,
        viewType: String,
        workflowName: String?,
        isBooleanAnswer: Boolean,
        triple: Triple<String, String, String>
    ): String {
        if (resultMap.containsKey(workflowName)) {
            val actualMap = resultMap[workflowName]
            if (actualMap is Map<*, *>) {
                val value = actualMap[id]
                if (viewType == ViewType.VIEW_TYPE_FORM_DATEPICKER && value is String) {
                    return DateUtils.convertDateFormat(
                        value,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DateUtils.DATE_ddMMyyyy
                    )
                } else if (viewType == ViewType.VIEW_TYPE_DIALOG_CHECKBOX) {
                    return getDangerSignValue(value, triple.third)
                } else {
                    when (value) {
                        is String -> {
                            return value
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
                            return df.format(value)
                        }
                    }
                }

            }
        }
        return triple.third
    }

    private fun getDangerSignValue(value: Any?, hyphenSymbol: String): String {
        val result = ArrayList<String>()
        if (value is List<*>) {
            value.forEach {
                if (it is Map<*, *>) {
                    val key = it[DefinedParams.NAME]
                    if (key is String) {
                        result.add(key)
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
        val weeks = diff / 7
        return (weeks.toDouble() / 4)
    }

    fun calculateNextANCVisitDate(lmp: Date): Date? {
        return when (calculatePregnancyMonth(lmp)) {
            in 0.0..4.0 -> {
                DateUtils.addDaysToDate(lmp, (28 * 5))
            }
            in 4.1..5.0 -> {
                DateUtils.addDaysToDate(lmp, (28 * 6))
            }
            in 5.1..6.0 -> {
                DateUtils.addDaysToDate(lmp, (28 * 7))
            }
            in 6.1..7.0 -> {
                DateUtils.addDaysToDate(lmp, (28 * 8))
            }
            in 7.1..8.9 -> {
                DateUtils.addDaysToDate(lmp, (28 * 9))
            }
            else -> {
                return null
            }
        }
    }

    fun calculateNextChildHoodVisitDate(age: Int, birthDate: Date): Date? {
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
            in 4..6 -> {
                return DateUtils.addDaysToDate(deliveryDate, 15)
            }
            else -> {
                return null
            }
        }
    }

    const val childHoodVisitMaxMonth = 15

    fun getMenuName(workflowName: String?): String {
        when (workflowName) {
            ANC -> return ANC_MENU.uppercase(Locale.getDefault())
            ChildHoodVisit -> return CHILD_MENU.uppercase(Locale.getDefault())
            PNC -> return PNC_MENU.uppercase(Locale.getDefault())
        }
        return MenuConstants.RMNCH_MENU_ID.uppercase(Locale.getDefault())
    }




}