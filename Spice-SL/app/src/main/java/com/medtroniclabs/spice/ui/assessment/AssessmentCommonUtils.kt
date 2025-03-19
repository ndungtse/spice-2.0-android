package com.medtroniclabs.spice.ui.assessment

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.No
import com.medtroniclabs.spice.common.DefinedParams.Yes
import com.medtroniclabs.spice.databinding.AssessmentSummaryLayoutBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.AssessmentSummaryModel

object AssessmentCommonUtils {
    fun getListItemValue(
        givenId: String,
        listSummaryData: MutableList<AssessmentSummaryModel>
    ): AssessmentSummaryModel? {
        return listSummaryData.find { it.id == givenId }
    }

    fun getValueOfKeyFromMap(stringToMap: Map<String, Any>, keys: String, menuType: String): String? {
        val mapDataByType = stringToMap[menuType.lowercase()] as Map<*, *>
        for (entry in mapDataByType.entries) {
            if (entry.value is Map<*, *>) {
                val nestedMap = entry.value as? Map<*, *>
                if (nestedMap?.containsKey(keys) == true) {
                    if (nestedMap[keys] is String) {
                        return nestedMap[keys] as String
                    }
                    if (nestedMap[keys] is Double) {
                        return nestedMap[keys].toString()
                    }
                    if (nestedMap[keys] is Boolean) {
                        return if (nestedMap[keys] == true) Yes else No
                    }
                    if (nestedMap[keys] is ArrayList<*>) {
                        val arrayListValue = nestedMap[keys] as ArrayList<*>
                        return getDialogValue(arrayListValue)
                    }
                }
            }
        }
        return null
    }

    private fun getDialogValue(value: Any?, otherSymptoms: String? = null): String {
        val result = StringBuilder()
        if (value is ArrayList<*>) {
            value.forEach { map ->
                CommonUtils.getListActual(map)?.let {
                    result.append(it)
                    result.append(", ")
                }
            }
        }
        if (result.isNotEmpty()) {
            otherSymptoms?.let {
                return result.delete(result.length - 2, result.length).append(" - ").append(it)
                    .toString()
            }
            return result.delete(result.length - 2, result.length).toString()
        }
        return ""
    }

    fun getListActual(map: Any?): String? {
        if (map is Map<*, *> && map.containsKey(DefinedParams.NAME)) {
            val actual = map[DefinedParams.NAME]
            if (actual is String)
                return actual
        }
        return null
    }

    fun addViewSummaryLayout(title: String?, value: String?, valueTextColor: Int? = null, context:Context, isCallShown:Boolean = false, callBtnTag : String? = null,   callback: ((String?,String?) -> Unit)? = null, forCbs:Boolean = false, countryCode:String? = null): ConstraintLayout {
        val summaryBinding = AssessmentSummaryLayoutBinding.inflate(LayoutInflater.from(context))
        summaryBinding.tvKey.text = title ?: context.getString(R.string.separator_hyphen)
        val formattedTitle = countryCode?.let { "$it $value" } ?: value
        summaryBinding.tvValue.text = getSummaryValue(context, formattedTitle)
        summaryBinding.tvValue.tag = value
        valueTextColor?.let {
            summaryBinding.tvValue.setTextColor(it)
            summaryBinding.tvValue.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        }
        summaryBinding.callButton.setVisible(isCallShown)
        if (forCbs) {
            val params = summaryBinding.tvKey.layoutParams as ConstraintLayout.LayoutParams
            params.horizontalWeight = 0.4f
            summaryBinding.tvKey.layoutParams = params
            val paramsSep =
                summaryBinding.tvRowSeparator.layoutParams as ConstraintLayout.LayoutParams
            paramsSep.horizontalWeight = 0.125f
            summaryBinding.tvRowSeparator.layoutParams = paramsSep
        }
        callBtnTag?.let {
            summaryBinding.callButton.tag = callBtnTag
            summaryBinding.callButton.safeClickListener {
                val tag = summaryBinding.callButton.tag as? String ?: ""
                val value = summaryBinding.tvValue.tag as? String ?: ""
                callback?.invoke(tag, value)
            }
        }
        return summaryBinding.root
    }

    private fun getSummaryValue(context: Context, input: String?): String {
        if (input !=null && input.trim().isNotEmpty()) {
            return input
        } else {
            return context.getString(R.string.separator_hyphen)
        }
    }

    fun getNutritionStatus(selectedId: String?, context: Context): String {
        return when (selectedId) {
            AssessmentDefinedParams.Green -> context.getString(R.string.normal)
            AssessmentDefinedParams.Yellow -> context.getString(R.string.moderate_malnutrition)
            AssessmentDefinedParams.Red -> context.getString(R.string.severe_nutrition)
            else -> context.getString(R.string.hyphen_symbol)
        }
    }
    fun getMuacColorCode(selectedId: String?, context: Context): Int {
        return when (selectedId) {
            AssessmentDefinedParams.Green -> context.getColor( R.color.bmi_normal_weight)
            AssessmentDefinedParams.Yellow -> context.getColor( R.color.bmi_over_weight)
            AssessmentDefinedParams.Red ->context.getColor( R.color.medium_high_risk_color)
            else ->context.getColor( R.color.edittext_stroke)
        }
    }
}