package com.medtroniclabs.spice.ui.medicalreview.prescription

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.data.MedicationResponse
import com.medtroniclabs.spice.databinding.MedicationSerachAdapterLayoutBinding

class MedicationSearchAdapter(context: Context) :
    ArrayAdapter<MedicationResponse>(context, R.layout.spinner_drop_down_item) {
    private var medicationList = ArrayList<MedicationResponse>()

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
    ): View {
        val view: View
        val viewHolder: ViewHolder
        if (convertView == null) {
            val binding = MedicationSerachAdapterLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
            view = binding.root
            viewHolder = ViewHolder(binding)
            viewHolder.bind(getItem(position))
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = convertView.tag as ViewHolder
            viewHolder.bind(getItem(position))
        }
        return view
    }

    class ViewHolder(val binding: MedicationSerachAdapterLayoutBinding) {
        fun bind(item: MedicationResponse) {
            if (item.isGroup) {
                val name = "${item.groupName}"
                binding.tvMedicationName.text = name
                binding.tvClassification.text = item.classificationName
            } else {
                val name = "${item.name}, ${item.brandName}"
                binding.tvMedicationName.text = name
                binding.tvClassification.text = item.classificationName
            }
        }
    }

    override fun getCount(): Int = medicationList.size

    override fun getItem(position: Int): MedicationResponse = medicationList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    fun setData(itemList: ArrayList<MedicationResponse>) {
        this.medicationList = itemList
    }
}
