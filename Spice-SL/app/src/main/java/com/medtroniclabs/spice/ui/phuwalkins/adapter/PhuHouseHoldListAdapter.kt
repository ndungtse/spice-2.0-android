package com.medtroniclabs.spice.ui.phuwalkins.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ListItemHouseholdBinding
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.phuwalkins.listener.PhuLinkCallback

class PhuHouseHoldListAdapter(
    private val households: List<HouseHoldEntityWithMemberCount>,
    private val listener: PhuLinkCallback
) :
    RecyclerView.Adapter<PhuHouseHoldListAdapter.HouseholdViewHolder>() {

    class HouseholdViewHolder(val binding: ListItemHouseholdBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseholdViewHolder {
        return HouseholdViewHolder(
            ListItemHouseholdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: HouseholdViewHolder, position: Int) {
        val item = households[position]
        holder.binding.tvCardHouseholdName.text = item.name
        holder.binding.tvHouseholdNo.text =
            item.householdNo?.toString() ?: holder.context.getString(
                R.string.separator_double_hyphen
            )
        holder.binding.tvVillageName.text = item.villageName

        holder.binding.cardPatient.safeClickListener {
            listener.onLinkClicked(item)
        }
    }

    override fun getItemCount(): Int = households.size
}