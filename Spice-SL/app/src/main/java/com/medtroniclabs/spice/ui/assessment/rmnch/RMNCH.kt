package com.medtroniclabs.spice.ui.assessment.rmnch

import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.formgeneration.config.ViewType
import java.text.DecimalFormat

object RMNCH {
    const val RMNCHChildHoodVisit = "RMNCHChildHoodVisit"
    const val PlaceOfDelivery = "placeOfDelivery"
    const val ANC = "anc"
    const val PNC = "pncMother"
    const val PNCNeonatal = "pncNeonatal"
    const val ChildHoodVisit = "pncChild"
    const val lastMenstrualPeriod = "lastMenstrualPeriod"
    const val visitNo = "visitNo"
    const val PNC_MENU = "pnc"
    const val ANC_MENU = "anc"
    const val CHILD_MENU = "ChildHood_Visit"
    const val DateOfDelivery = "dateOfDelivery"
    const val NoOfNeonate = "noOfNeonate"
    const val gestationalAge = "gestationalAge"
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
                    return getDangerSignValue(value,triple.third)
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
}