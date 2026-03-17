package org.medtroniclabs.uhis.ui.medicalreview.epi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.databinding.RowMissedVaccinationItemBinding
import org.medtroniclabs.uhis.databinding.RowMissedVaccinationItemHeaderBinding
import org.medtroniclabs.uhis.model.medicalreview.VaccinationDetail

class MissedVaccinationAdapter(private val list: List<VaccinationDetail>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class HeaderViewHolder(val binding: RowMissedVaccinationItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ItemViewHolder(val binding: RowMissedVaccinationItemBinding) :
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
                RowMissedVaccinationItemHeaderBinding.inflate(
                    LayoutInflater.from(
                        parent.context,
                    ),
                    parent,
                    false,
                ),
            )
        } else {
            return ItemViewHolder(
                RowMissedVaccinationItemBinding.inflate(
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
                itemHolder.binding.clRootMissedVaccineItem.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.context,
                        R.color.table_row_color,
                    ),
                )
            } else {
                itemHolder.binding.clRootMissedVaccineItem.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.context,
                        R.color.white,
                    ),
                )
            }

            val vaccinationDetail = list[position]
            itemHolder.binding.tvVaccinationName.text = vaccinationDetail.vaccineName

            if (vaccinationDetail.vaccinatedDate != null) {
                itemHolder.binding.ivStatus.setImageResource(R.drawable.success_icon)
            } else {
                itemHolder.binding.ivStatus.setImageResource(R.drawable.missed_icon)
            }
        }
    }
}
