package com.medtroniclabs.spice.common

import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.google.gson.Gson
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.convertDateFormat
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.Screening

object FormAutofill {
    fun start(formGenerator: FormGenerator, values: Any?) {
        values?.let {
            val resultMap = objectToMap(it)
            resultMap.forEach { map ->
                (map.key as? String?)?.let { key ->
                    formGenerator.getViewByTag(key)?.let { view ->
                        when (view) {
                            is EditText -> {
                                setEditText(view, map.value)
                            }

                            is Spinner -> {
                                setSpinner(view, map.value)
                            }

                            is SingleSelectionCustomView -> {
                                setSingleSelectionCustomView(view, key, map.value)
                            }

                            is TextView -> {
                                setTextView(view, key, map.value, formGenerator)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setTextView(
        view: TextView,
        key: String,
        value: Any?,
        formGenerator: FormGenerator
    ) {
        if (value is String) {
            if (key.equals(Screening.DateOfBirth, true)) {
                val dobString =
                    convertDateFormat(value, DATE_FORMAT_yyyyMMddHHmmssZZZZZ, DATE_ddMMyyyy)
                val dobDate = DateUtils.convertStringToDate(value, DATE_FORMAT_yyyyMMddHHmmssZZZZZ)

                if (dobString.isNotBlank())
                    view.text = dobString

                if (dobDate != null)
                    formGenerator.fillDetailsOnDatePickerSet(dobDate, false, id = key)
            } else
                view.text = value
        }
    }

    private fun setSingleSelectionCustomView(
        view: SingleSelectionCustomView,
        key: String,
        value: Any?
    ) {
        when (value) {
            is String -> view.singleSelectionChildViewsOption(value)
            is Boolean -> {
                val id = "${value}_$key"
                view.singleSelectionChildViewsOption(value, id)
            }
        }
    }

    private fun setSpinner(view: Spinner, value: Any?) {
        val adapter = view.adapter
        if (adapter is CustomSpinnerAdapter) {
            when (value) {
                is String -> view.setSelection(adapter.getIndexOfItemById(value))
            }
        }
    }

    private fun setEditText(view: EditText, value: Any?) {
        if (value is String) view.setText(value)
    }


    private fun objectToMap(obj: Any): Map<*, *> {
        val gson = Gson()
        return gson.fromJson(gson.toJson(obj), Map::class.java) as Map<*, *>
    }
}