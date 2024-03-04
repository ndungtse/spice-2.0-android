package com.medtroniclabs.spice.ui.household.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ListItemHouseholdBinding
import com.medtroniclabs.spice.db.response.HouseHoldEntityWithMemberCount
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.household.HouseholdSelectionListener

class HouseholdListAdapter(
    private val listener: HouseholdSelectionListener,
    private val houseHoldList: ArrayList<HouseHoldEntityWithMemberCount>
) : RecyclerView.Adapter<HouseholdListAdapter.HouseholdListViewHolder>() {

    inner class HouseholdListViewHolder(val binding: ListItemHouseholdBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onBindViewHolder(
        holder: HouseholdListViewHolder,
        position: Int
    ) {
        val item = houseHoldList[position]
        holder.binding.tvCardHouseholdName.text = item.name
        holder.binding.tvHouseholdNo.text = item.householdNo.toString()
        val membersText = holder.context.getString(
            R.string.people_registered,
            item.registerMemberCount,
            item.noOfPeople
        )
        holder.binding.tvMembersRegistered.text = membersText
        holder.binding.ivMemberRegCount.visibility =
            if (item.noOfPeople == item.registerMemberCount) View.INVISIBLE else View.VISIBLE
        holder.binding.cardPatient.safeClickListener {
            listener.onHouseHoldSelected(item.id)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HouseholdListViewHolder {
        return HouseholdListViewHolder(
            ListItemHouseholdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return if (houseHoldList.isNullOrEmpty()) {
            1
        } else {
            houseHoldList.size
        }
    }

}