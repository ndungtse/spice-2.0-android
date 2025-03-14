package com.medtroniclabs.spice.formgeneration.utility

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.db.entity.SignsAndSymptomsEntity
import com.medtroniclabs.spice.formgeneration.config.DefinedParams
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener

class CheckboxDialogAdapter(
    private val dialogList: List<SignsAndSymptomsEntity>,
    val translate: Boolean = false
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    inner class DialogHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBoxHeader: TextView = itemView.findViewById(R.id.checkboxItemHeader)
    }

    inner class DialogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkboxItem)
        val Root: LinearLayout = itemView.findViewById(R.id.root)
    }

    override fun getItemViewType(position: Int): Int {
        return if (dialogList[position].isTitle) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.checkbox_dialog_header, parent, false)
            DialogHeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.checkbox_dialog_items, parent, false)
            DialogViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = dialogList[position]
        if (holder is DialogHeaderViewHolder) {
            holder.checkBoxHeader.text =
                if (translate) item.displayValue ?: item.symptom else item.symptom
        } else if (holder is DialogViewHolder) {
            holder.checkBox.text =
                if (translate) item.displayValue ?: item.symptom else item.symptom
            holder.checkBox.isChecked = item.isSelected
            holder.checkBox.isEnabled = item.isEnabled
            holder.Root.safeClickListener {
            if (item.isEnabled) {
                checkDataAndUpdate(item, dialogList)
            }
        }
    }

    }

    private fun checkDataAndUpdate(
        item: SignsAndSymptomsEntity,
        dialogList: List<SignsAndSymptomsEntity>
    ) {
        if (item.symptom.startsWith(DefinedParams.NoSymptoms, true)) {
            updateNoSymptomSelection(item, dialogList)
        } else {
            val model = dialogList.find { it.symptom.startsWith(DefinedParams.NoSymptoms, true) }
            model?.isSelected = false
            item.isSelected = !item.isSelected
        }
        notifyDataSetChanged()
    }

    private fun updateNoSymptomSelection(
        item: SignsAndSymptomsEntity,
        dialogList: List<SignsAndSymptomsEntity>
    ) {
        dialogList.forEach {
            if (it._id == item._id) {
                it.isSelected = !it.isSelected
            } else {
                it.isSelected = false
            }
        }
    }

    override fun getItemCount(): Int = dialogList.size
    fun getSelectedItems(): ArrayList<HashMap<String, Any>> {
        val list = dialogList.filter { it.isSelected }
        val selectedItemList = ArrayList<HashMap<String, Any>>()
        list.forEach {
            val map = HashMap<String, Any>()
            map[DefinedParams.ID] = it._id
            map[DefinedParams.NAME] = it.symptom
            it.displayValue?.let { cultureValue ->
                map[DefinedParams.cultureValue] = cultureValue
            }
            it.value?.let { value ->
                map[DefinedParams.value] = value
            }
            selectedItemList.add(map)
        }
        return selectedItemList
    }
}