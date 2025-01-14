package com.medtroniclabs.spice.common

import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.convertDateFormat
import com.medtroniclabs.spice.formgeneration.FormGenerator
import com.medtroniclabs.spice.formgeneration.MentalHealthAdapter
import com.medtroniclabs.spice.formgeneration.SingleSelectionMHView
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.mappingkey.Screening

object FormAutofill {
    fun start(formGenerator: FormGenerator, values: Any) {
        val resultMap = objectToMap(values)
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

                        is RecyclerView -> {
                            if (view.adapter is MentalHealthAdapter)
                                (map.value as? ArrayList<Map<String, Any>>)?.let { list ->
                                    prefillRecyclerView(view, list)
                                }
                        }
                    }
                }
            }
        }
    }

    private fun prefillRecyclerView(view: RecyclerView, list: ArrayList<Map<String, Any>>) {
        for (i in 0 until list.size) {
            //Getting one by one listItem
            (view.findViewHolderForAdapterPosition(i) as? MentalHealthAdapter.MentalHealthViewHolder)?.let { viewHolder ->
                val questionView = viewHolder.binding.tvQuestion
                val inflatedView = viewHolder.binding.llAnswerRoot

                val map = list.firstOrNull {
                    (it[Screening.MHQuestion] as? String)?.equals(
                        questionView.text?.toString(),
                        true
                    ) == true
                }

                if (!map.isNullOrEmpty())
                    for (j in 0 until inflatedView.childCount) {
                        (inflatedView.getChildAt(j) as? SingleSelectionMHView)?.let { singleSelectionView ->
                            (map[Screening.MHAnswer] as? String)?.let {
                                singleSelectionView.prefillSingleSelection(it)
                            }
                        }
                    }
            }
        }
    }

    private fun setTextView(
        view: TextView, key: String, value: Any?, formGenerator: FormGenerator
    ) {
        if (value is String) {
            if (key.equals(Screening.DateOfBirth, true)) {
                val dobString =
                    convertDateFormat(value, DATE_FORMAT_yyyyMMddHHmmssZZZZZ, DATE_ddMMyyyy)
                val dobDate = DateUtils.convertStringToDate(value, DATE_FORMAT_yyyyMMddHHmmssZZZZZ)

                if (dobString.isNotBlank()) view.text = dobString

                if (dobDate != null) formGenerator.fillDetailsOnDatePickerSet(
                    dobDate,
                    false,
                    id = key
                )
            } else view.text = value
        }
    }

    private fun setSingleSelectionCustomView(
        view: SingleSelectionCustomView, key: String, value: Any?
    ) {
        when (value) {
            is String -> view.singleSelectionChildViewsOption(value)
            is Boolean -> {
                val id = "${value}_$key"
                view.singleSelectionAutofill(id)
            }
        }
    }

    private fun setSpinner(view: Spinner, value: Any?) {
        val adapter = view.adapter
        if (adapter is CustomSpinnerAdapter) {
            when (value) {
                is String -> {
                    val index = adapter.getIndexOfItemById(value)
                    if (index > 0)
                        view.setSelection(index)
                }
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