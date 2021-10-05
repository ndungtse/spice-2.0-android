package com.mdtlabs.ncd.appextensions

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.google.android.material.textfield.TextInputLayout
import com.mdtlabs.ncd.R
import com.mdtlabs.ncd.common.ViewUtil.getResId

fun TextInputLayout.markMandatory() {
    hint ?: return
    hint = buildSpannedString {
        append(hint)
        color(Color.RED) { append(" *") } // Mind the space prefix.
    }
}

fun TextView.markMandatory() {
    text = buildSpannedString {
        append(text)
        color(Color.RED) { append(" *") } // Mind the space prefix.
    }
}

var TextView.textSizeSsp: Int?
    get() {
        return textSize.toInt()
    }
    set(size) {
        size ?: return
        val sizeString = "_${size}ssp"
        val resId = getResId(sizeString, R.dimen::class.java)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelOffset(resId).toFloat())
    }

/**
 * Disable the copy paste value for edit text
 */
fun TextView.disableCopyPaste() {
    isLongClickable = false
    setTextIsSelectable(false)
    customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
        override fun onActionItemClicked(p0: android.view.ActionMode?, p1: MenuItem?): Boolean {
            return false
        }

        override fun onCreateActionMode(p0: android.view.ActionMode?, p1: Menu?): Boolean {
            return false
        }

        override fun onPrepareActionMode(p0: android.view.ActionMode?, p1: Menu?): Boolean {
            return false
        }

        override fun onDestroyActionMode(p0: android.view.ActionMode?) {
        }
    }
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.ssp: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}