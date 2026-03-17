package org.medtroniclabs.uhis.ui.medicalreview.epi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.databinding.RowEpiCatchUpPolicyHeaderBinding
import org.medtroniclabs.uhis.databinding.RowEpiCatchUpWorkPolicyItemBinding
import org.medtroniclabs.uhis.model.medicalreview.EpiCatchUpPolicyItem

class EpiCatchUpPolicyAdapter(private val list: List<EpiCatchUpPolicyItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class HeaderViewHolder(val binding: RowEpiCatchUpPolicyHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ItemViewHolder(val binding: RowEpiCatchUpWorkPolicyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        if (viewType == 0) {
            return HeaderViewHolder(
                RowEpiCatchUpPolicyHeaderBinding.inflate(
                    LayoutInflater.from(
                        parent.context,
                    ),
                    parent,
                    false,
                ),
            )
        } else {
            return ItemViewHolder(
                RowEpiCatchUpWorkPolicyItemBinding.inflate(
                    LayoutInflater.from(
                        parent.context,
                    ),
                    parent,
                    false,
                ),
            )
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (position != 0) {
            val itemHolder = holder as ItemViewHolder

            if (position % 2 == 0) {
                itemHolder.binding.clRootCatchUpPolicyItem.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.context,
                        R.color.table_row_color,
                    ),
                )
            } else {
                itemHolder.binding.clRootCatchUpPolicyItem.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.context,
                        R.color.white,
                    ),
                )
            }

            val vaccinationDetail = list[position]
            itemHolder.binding.tvVaccinationName.text = vaccinationDetail.vaccineName
            itemHolder.binding.tvMinAge.text = vaccinationDetail.minimumAge
            itemHolder.binding.tvMaxAge.text = vaccinationDetail.maximumAge
            itemHolder.binding.tvIntervalAndDose.text = vaccinationDetail.numberAndInterval
        }
    }
}
