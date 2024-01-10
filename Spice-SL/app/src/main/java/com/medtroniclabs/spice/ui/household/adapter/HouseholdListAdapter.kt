package com.medtroniclabs.spice.ui.household.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.safeClickListener
import com.medtroniclabs.spice.db.entity.HouseholdEntity
import com.medtroniclabs.spice.databinding.ListItemHouseholdBinding
import com.medtroniclabs.spice.ui.household.HouseholdSelectionListener

class HouseholdListAdapter(
    val listener: HouseholdSelectionListener,
    private val houseHoldList: ArrayList<HouseholdEntity>
) : RecyclerView.Adapter<HouseholdListAdapter.HouseholdListViewHolder>() {

    inner class HouseholdListViewHolder(val binding: ListItemHouseholdBinding) :
        RecyclerView.ViewHolder(binding.root){
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
            item.noOfPeopleRegistered,
            item.noOfPeople
        )
        holder.binding.tvMembersRegistered.text = membersText
        holder.binding.ivMemberRegCount.visibility =
            if (item.noOfPeople == item.noOfPeopleRegistered) View.INVISIBLE else View.VISIBLE
        houseHoldList[position].let { model ->
            holder.binding.cardPatient.safeClickListener {
                listener.onSelectedPatient(model)
            }
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