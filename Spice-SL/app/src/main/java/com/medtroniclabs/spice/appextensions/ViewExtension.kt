package com.medtroniclabs.spice.appextensions

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.spice.common.CommonUtils

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun EditText.setTextChangeListener(
    onChanged: (text: String?) -> Unit
) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
            // Not used, implementation can be left empty
        }

        override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
            // Not used, implementation can be left empty
        }

        override fun afterTextChanged(s: Editable?) {
            onChanged(s?.toString())
        }
    })
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.isVisible(): Boolean {
    return this.visibility == View.VISIBLE
}

fun View.isGone() : Boolean{
    return this.visibility == View.GONE
}

fun View.isInvisible(): Boolean {
    return this.visibility == View.INVISIBLE
}

fun <T> ArrayList<T>.nullIfEmpty(): ArrayList<T>? {
    return if (this.isEmpty()) {
        null
    } else {
        this
    }
}


fun DialogFragment.setWidth(percentage: Int) {
    val percent = percentage.toFloat() / 100
    val dm = Resources.getSystem().displayMetrics
    val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
    val percentWidth = rect.width() * percent
    dialog?.window?.setLayout(percentWidth.toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
}


fun ConstraintLayout.setPercentWidth(viewId: Int, percentage: Float) {
    val constraintSet = ConstraintSet()
    constraintSet.clone(this)
    constraintSet.constrainPercentWidth(viewId, percentage)
    constraintSet.applyTo(this)
}

fun Context.isNotTabletAndPortrait(): Boolean {
    val orientation = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    return !CommonUtils.checkIsTablet(this) && orientation
}

fun DialogFragment.setDialogWidthAndHeightAsWrapPercent(widthPercent: Int) {
    val widthPercentage = widthPercent.toFloat() / 100
    val dm = Resources.getSystem().displayMetrics
    val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
    val percentWidth = rect.width() * widthPercentage
    dialog?.window?.setLayout(percentWidth.toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
}
