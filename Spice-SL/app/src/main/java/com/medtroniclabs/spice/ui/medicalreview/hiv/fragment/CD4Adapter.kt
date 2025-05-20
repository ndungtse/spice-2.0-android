package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.toCleanString
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.DATE_ddMMyyyy
import com.medtroniclabs.spice.common.DateUtils.convertDateFormat
import com.medtroniclabs.spice.data.resource.CD4DetailsResponse
import com.medtroniclabs.spice.databinding.ItemCd4RowBinding

class CD4Adapter(private val cd4List: List<CD4DetailsResponse>) :
    RecyclerView.Adapter<CD4Adapter.CD4ViewHolder>() {

    class CD4ViewHolder(val binding: ItemCd4RowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(response: CD4DetailsResponse, cd4List: List<CD4DetailsResponse>) {
            binding.tvDate.text = response.dateValue?.let {
                convertDateFormat(
                    it,
                    DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DATE_ddMMyyyy
                )
            } ?: binding.root.context.getString(R.string.separator_double_hyphen)
            binding.tvCd4Percentage.text = response.doubleValue?.toCleanString()
                ?: binding.root.context.getString(R.string.separator_double_hyphen)
            if (bindingAdapterPosition == cd4List.size - 1) {
                binding.divider.visibility = View.GONE
            } else {
                binding.divider.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CD4ViewHolder {
        return CD4ViewHolder(
            ItemCd4RowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CD4ViewHolder, position: Int) {
        return holder.bind(cd4List[position], cd4List)
    }

    override fun getItemCount(): Int = cd4List.size
}