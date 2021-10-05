package com.mdtlabs.ncd.common

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import java.lang.reflect.Field
import java.util.*


object ViewUtil {


    fun getResId(resName: String, c: Class<*>): Int {
        return try {
            val idField: Field = c.getDeclaredField(resName)
            idField.getInt(idField)
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    fun showDatePicker(
        context: Context,
        disableFutureDate: Boolean = false,
        minDate: Long? = null,
        maxDate: Long? = null,
        callBack: (dialog: DatePicker, year: Int, month: Int, dayOfMonth: Int) -> Unit,
    ): DatePickerDialog {

        val calendar = Calendar.getInstance()
        val thisYear = calendar.get(Calendar.YEAR)
        val thisMonth = calendar.get(Calendar.MONTH)
        val thisDay = calendar.get(Calendar.DAY_OF_MONTH)
        val dialog: DatePickerDialog?

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { datePicker, year, month, dayOfMonth ->
                callBack.invoke(datePicker, year, month + 1, dayOfMonth)
            }

        dialog = DatePickerDialog(
            context,
            dateSetListener,
            thisYear,
            thisMonth,
            thisDay
        )

        minDate?.let {
            dialog.datePicker.minDate = it
        }
        maxDate?.let {
            dialog.datePicker.maxDate = it
        }

        if (disableFutureDate) dialog.datePicker.maxDate = System.currentTimeMillis()

        dialog.show()

        return dialog

    }
}