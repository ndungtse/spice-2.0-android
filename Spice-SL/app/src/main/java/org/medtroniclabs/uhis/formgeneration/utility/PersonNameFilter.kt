package org.medtroniclabs.uhis.formgeneration.utility

import android.text.InputFilter
import android.text.Spanned

/**
 * Person name input filter
 */
class PersonNameFilter : InputFilter {
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int,
    ): CharSequence {
        val newText = StringBuilder(dest).replace(dstart, dend, source.subSequence(start, end).toString()).toString()
        val regex = Regex(PATTERN)
        return if (regex.matches(newText)) source else ""
    }

    companion object {
        private const val PATTERN = "^[A-Za-z .'-]*$"
        const val VALIDATION_PATTERN = "^[A-Za-z]+([ .'-]+[A-Za-z]+)*$"
    }
}
