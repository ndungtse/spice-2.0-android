package com.medtroniclabs.spice.ui.medicalreview.epi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.databinding.RowImmunisationCardBinding
import com.medtroniclabs.spice.model.medicalreview.VaccinationDetail
import com.medtroniclabs.spice.model.medicalreview.VaccinationGroupItem
import com.medtroniclabs.spice.ui.medicalreview.epi.view.VaccinationItemView

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

    fun refreshAdapterList() {
        notifyItemRangeChanged(0, items.size)
    }

    fun getVaccinationGroupItems(): List<VaccinationGroupItem> = items

    inner class ImmunisationListViewHolder(val binding: RowImmunisationCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ImmunisationListViewHolder =
        ImmunisationListViewHolder(
            RowImmunisationCardBinding.inflate(
                LayoutInflater.from(
                    parent.context,
                ),
                parent,
                false,
            ),
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(
        holder: ImmunisationListViewHolder,
        position: Int,
    ) {
        val ctx = holder.itemView.context
        val groupItem = items[position]
        holder.binding.tvImmunisationCategory.text = groupItem.groupName

        holder.binding.flVaccinationItems.removeAllViews()
        groupItem.vaccinationItems.forEach { item ->
            val view = VaccinationItemView(ctx, item = item, callback = callback)
            holder.binding.flVaccinationItems.addView(view)
        }

       /* val dayDiff = ChronoUnit.DAYS.between(groupItem.scheduleDate.getLocalDate(), LocalDate.now())
        if (dayDiff < 0)*/
    }
}
