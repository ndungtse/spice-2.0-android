package org.medtroniclabs.uhis.formgeneration.utility

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.isVisible
import org.medtroniclabs.uhis.data.model.DosageTableModel
import org.medtroniclabs.uhis.databinding.TableRowLayoutBinding

class TableRowAdapter(
    val context: Context,
    private val infoList: ArrayList<DosageTableModel>,
) : RecyclerView.Adapter<TableRowAdapter.InformationListViewHolder>() {
    inner class InformationListViewHolder(val binding: TableRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): TableRowAdapter.InformationListViewHolder =
        InformationListViewHolder(
            TableRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )

    override fun onBindViewHolder(
        holder: InformationListViewHolder,
        position: Int,
    ) {
        val infoModel = infoList[position]
        if (position % 2 != 0) {
            holder.binding.apply {
                tvDay.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.context,
                        R.color.table_row_color,
                    ),
                )
                tvMorning.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.context,
                        R.color.table_row_color,
                    ),
                )
                tvNight.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.context,
                        R.color.table_row_color,
                    ),
                )
            }
        }
        holder.binding.tvDay.text = infoModel.day
        holder.binding.tvMorning.text = infoModel.morning
        infoModel.night?.let {
            holder.binding.tvNight.text = infoModel.night
        } ?: kotlin.run {
            if (holder.binding.tvNight.isVisible()) {
                holder.binding.tvNight.gone()
            }
        }
    }

    override fun getItemCount(): Int = infoList.size
}
