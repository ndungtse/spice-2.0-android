package com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc

import android.content.Context
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doAfterTextChanged
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible

object MotherNeonateUtil {
    fun convertNullableIntToString(value: Int?, context: Context): String {
        return value?.toString() ?: context.getString(R.string.hyphen_symbol)
    }

    fun convertNullableDoubleToString(value: Double?, context: Context): String {
        return value?.toString() ?: context.getString(R.string.hyphen_symbol)
    }

    fun convertNullableStringToString(value: String?, context: Context): String {
        return value?.takeIf { it.isNotBlank() } ?: context.getString(R.string.hyphen_symbol)
    }

    fun initTextWatcherForDouble(
        view: AppCompatEditText,
        propertySetter: (Double?) -> Unit
    ) {
        view.doAfterTextChanged {
            val text = it.toString().trim()
            val value = if (text.isNotBlank()) text.toDoubleOrNull() else null
            propertySetter(value)
        }
    }

    fun initTextWatcherForInt(view: AppCompatEditText, propertySetter: (Int?) -> Unit) {
        view.doAfterTextChanged {
            val text = it.toString().trim()
            val value = if (text.isNotBlank()) text.toIntOrNull() else null
            propertySetter(value)
        }
    }

    fun isValidInput(
        inputText: String,
        editText: EditText,
        errorTextView: TextView,
        validRange: ClosedRange<Double>,
        errorMessageResId: Int,
        context: Context
    ): Boolean {
        val input = inputText.toDoubleOrNull()
        if (editText.text.isNullOrBlank()) {
            errorTextView.gone()
            return true
        }
        if (!(input != null && input in validRange)) {
            errorTextView.visible()
            errorTextView.text = context.getString(errorMessageResId)
            return false
        }
        errorTextView.gone()
        return true
    }

    fun isValidMeasurement(
        valueText: String?,
        errorTextView: TextView,
        minValue: Int,
        maxValue: Int,
        text: AppCompatEditText? = null,
        minErrorMessage: String,
        maxErrorMessage: String,
        context: Context
    ): Boolean {
        val value = valueText?.toIntOrNull()
        val diastolic = text?.text.toString().toIntOrNull()
        if (value == null) {
            // Invalid input, display error message
            errorTextView.gone()
            return true
        }
        if (value < minValue) {
            // Value is less than minimum allowed value, display error message
            errorTextView.text = minErrorMessage
            errorTextView.visible()
            return false
        }
        if (value > maxValue) {
            errorTextView.text = maxErrorMessage
            errorTextView.visible()
            return false
        }

        if (diastolic != null && value < diastolic) {
            errorTextView.text = context.getText(R.string.systolic_diastolic_error)
            errorTextView.visible()
            return false
        }
        // Valid input
        errorTextView.gone()
        return true
    }

    fun isBasicValid(
        valueText: String?,
        errorTextView: TextView,
        minValue: Int,
        errorMessage: String,
        maxValue: Int? = null,
        maxValueError: String? = null,
        context: Context
    ): Boolean {
        val value = valueText?.toIntOrNull()
        if (value == null) {
            // Invalid input, display error message
            errorTextView.gone()
            return true
        }
        if (value == minValue) {
            // Value is less than minimum allowed value, display error message
            errorTextView.text = errorMessage
            errorTextView.visible()
            return false
        }

        if (maxValue != null && value > maxValue) {
            errorTextView.text = maxValueError ?: context.getString(R.string.error)
            errorTextView.visible()
            return false
        }
        // Valid input
        errorTextView.gone()
        return true
    }

    fun isDataValid(value: Double?, view: TextView): Boolean {
        if (value == null) {
            view.gone()
            return true
        }
        if (value <= 0) {
            view.visible()
            return false
        }
        view.gone()
        return true
    }
}