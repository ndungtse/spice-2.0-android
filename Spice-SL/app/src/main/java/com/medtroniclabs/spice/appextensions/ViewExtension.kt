package com.medtroniclabs.spice.appextensions

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.medtroniclabs.spice.common.SafeClickListener

fun TextView.markMandatory() {
    text = buildSpannedString {
        append(text)
        color(Color.RED) { append(" *") } // Mind the space prefix.
    }
}

fun View.safeClickListener(clickListener: View.OnClickListener?) {
    val safeClickListener = SafeClickListener(clickListener)
    setOnClickListener(safeClickListener)
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}