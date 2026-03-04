package com.medtroniclabs.spice.formgeneration.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.formgeneration.config.DefinedParams

class MultiSelectSpinnerAdapter(
    context: Context,
    private val items: ArrayList<Map<String, Any>>,
    private val selectedItems: ArrayList<Map<String, Any>>,
) : ArrayAdapter<Map<String, Any>>(context, 0, items) {
    private val checkedItems = BooleanArray(items.size)
    private var onItemSelectedListener: OnItemSelectedListener? = null

    interface OnItemSelectedListener {
        fun onItemSelected(
            selectedItems: List<Map<String, Any>>,
            actionItem: Map<String, Any>?,
            isDeselect: Boolean,
        )
    }

    init {
        for (i in items.indices) {
            checkedItems[i] = selectedItems.contains(items[i])
        }
    }

    fun setOnItemSelectedListener(listener: OnItemSelectedListener?) {
        this.onItemSelectedListener = listener
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View = createView(position, convertView, parent, false)

    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View = createView(position, convertView, parent, true)

    private fun createView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
        isDropDown: Boolean,
    ): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(
            if (isDropDown) R.layout.custom_spinner_dropdown_item else R.layout.custom_non_select_dropdown,
            parent,
            false,
        )

        val textView = view.findViewById<TextView>(R.id.spin_txt)

        if (isDropDown) {
            val checkBox = view.findViewById<CheckBox>(R.id.spinnerCheckbox)
            val itemName = view.findViewById<TextView>(R.id.itemName)

            itemName.text = getCultureValue(items[position])
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = checkedItems[position]

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                checkedItems[position] = isChecked
                if (isChecked) {
                    val isResetItem =
                        items[position].containsKey("reset") && items[position]["reset"] as Boolean
                    val isSingleResetItem =
                        selectedItems.size == 1 && selectedItems[0].containsKey("reset") && selectedItems[0]["reset"] as Boolean
                    if (isResetItem || isSingleResetItem) {
                        selectedItems.clear()
                        checkedItems.forEachIndexed { index, _ ->
                            if (index != position) {
                                checkedItems[index] = false
                            }
                        }
                    }

                    if (!selectedItems.contains(items[position])) {
                        selectedItems.add(items[position])
                    }
                } else {
                    selectedItems.remove(items[position])
                }
                onItemSelectedListener?.onItemSelected(selectedItems, items[position], !isChecked)
                notifyDataSetChanged()
            }

            itemName.setOnClickListener {
                val newCheckedState = !checkBox.isChecked
                checkBox.isChecked = newCheckedState
            }
        } else {
            if (selectedItems.isEmpty()) {
                textView.text = context.getString(R.string.please_select)
            } else {
                val names = ArrayList<String>()
                for (i in getSelectedItems().indices) {
                    names.add(getCultureValue(getSelectedItems()[i]))
                }
                textView.text = if (names.size > 1) {
                    names.joinToString(", ")
                } else {
                    names[0]
                }
            }
        }

        return view
    }

    private fun getCultureValue(map: Map<String, Any>): String =
        if (SecuredPreference.getIsTranslationEnabled()) {
            map[DefinedParams.cultureValue] as? String ?: map[DefinedParams.NAME] as String
        } else {
            map[DefinedParams.NAME] as String
        }

    private fun getSelectedItems(): List<Map<String, Any>> = selectedItems

    fun reset() {
        selectedItems.clear()
        checkedItems.fill(false)

        // Update adapter
        notifyDataSetChanged()
        onItemSelectedListener?.onItemSelected(selectedItems, null, true)
    }
}
