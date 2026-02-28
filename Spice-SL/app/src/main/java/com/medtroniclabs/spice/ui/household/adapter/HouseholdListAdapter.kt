package com.medtroniclabs.spice.ui.household.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.databinding.ListItemHouseholdBinding
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithLastActivity
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener

/**
 * Showing list of households based from local DB in [com.medtroniclabs.spice.ui.household.HouseholdSearchActivity]
 */
class HouseholdListAdapter(
    private val callback: (Long) -> Unit,
) : RecyclerView.Adapter<HouseholdListAdapter.HouseholdListViewHolder>() {
    private val houseHoldList = mutableListOf<HouseHoldEntityWithLastActivity>()

    fun setHouseHoldList(list: List<HouseHoldEntityWithLastActivity>) {
        val oldCount = houseHoldList.size
        houseHoldList.clear()
        houseHoldList.addAll(list)
        notifyItemRangeRemoved(0, oldCount)
        notifyItemRangeInserted(0, list.size)
    }

    inner class HouseholdListViewHolder(val binding: ListItemHouseholdBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onBindViewHolder(
        holder: HouseholdListViewHolder,
        position: Int,
    ) {
        val item = houseHoldList[position]
        holder.binding.tvCardHouseholdName.text = item.name
        holder.binding.tvHouseholdNo.text = item.householdNo ?: holder.context.getString(R.string.separator_double_hyphen)
        holder.binding.tvLabelVillage.setText(R.string.village)
        holder.binding.tvVillageName.text = item.subVillageName
        holder.binding.tvSSName.text = item.shasthyaShebikaName
        holder.binding.tvLastVisitDate.text = DateUtils.formatDateToDisplayFormat(item.lastActivityAt)
        holder.binding.cardPatient.safeClickListener {
            callback(item.id)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HouseholdListViewHolder =
        HouseholdListViewHolder(
            ListItemHouseholdBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )

    override fun getItemCount(): Int = houseHoldList.size
}
