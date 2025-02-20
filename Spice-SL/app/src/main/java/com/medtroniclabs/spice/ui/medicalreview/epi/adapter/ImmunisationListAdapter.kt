package com.medtroniclabs.spice.ui.medicalreview.epi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.appextensions.getLocalDate
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.databinding.RowImmunisationCardBinding
import com.medtroniclabs.spice.model.medicalreview.VaccinationDetail
import com.medtroniclabs.spice.model.medicalreview.VaccinationGroupItem
import com.medtroniclabs.spice.ui.medicalreview.epi.view.VaccinationItemView
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ImmunisationListAdapter(private val callback: (VaccinationDetail) -> Unit) :
    RecyclerView.Adapter<ImmunisationListAdapter.ImmunisationListViewHolder>() {

    private val items = mutableListOf<VaccinationGroupItem>()

    fun setVaccinationGroupItems(list: List<VaccinationGroupItem>) {
        val oldCount = items.size
        items.clear()
        items.addAll(list)
        notifyItemRangeRemoved(0, oldCount)
        notifyItemRangeInserted(0, list.size)
    }

    fun getVaccinationGroupItems(): List<VaccinationGroupItem> {
        return items
    }

    inner class ImmunisationListViewHolder(val binding: RowImmunisationCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImmunisationListViewHolder {
        return ImmunisationListViewHolder(
            RowImmunisationCardBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ImmunisationListViewHolder, position: Int) {
        val ctx = holder.itemView.context
        val groupItem = items[position]
        holder.binding.tvImmunisationCategory.text = groupItem.groupName

        holder.binding.flVaccinationItems.removeAllViews()
        groupItem.vaccinationItems.forEach { item ->
            val view = VaccinationItemView(ctx, item = item, callback = callback)
            holder.binding.flVaccinationItems.addView(view)
        }

        holder.binding.flTransparentView.gone()
        val dayDiff = ChronoUnit.DAYS.between(groupItem.scheduleDate.getLocalDate(), LocalDate.now())
        if (dayDiff < 0)
            holder.binding.flTransparentView.visible()
    }
}