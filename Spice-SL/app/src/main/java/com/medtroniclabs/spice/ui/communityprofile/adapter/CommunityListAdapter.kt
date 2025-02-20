package com.medtroniclabs.spice.ui.communityprofile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.data.community.CommunityProfile
import com.medtroniclabs.spice.databinding.ItemCommunityProfileBinding

class CommunityListAdapter(
    private val selectedItem: (CommunityProfile) -> Unit
) : RecyclerView.Adapter<CommunityListAdapter.CommunityViewHolder>() {

    private val communityList = mutableListOf<CommunityProfile>()

    fun updateList(newCommunityList: List<CommunityProfile>) {
        val oldCount = communityList.size
        communityList.clear()
        communityList.addAll(newCommunityList)
        notifyItemRangeRemoved(0, oldCount)
        notifyItemRangeInserted(0, newCommunityList.size)
    }

    inner class CommunityViewHolder(private val binding: ItemCommunityProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(profile: CommunityProfile) {
            binding.tvCommunityName.text = profile.villageName
            binding.tvNoOfHouseholdCount.text = " ".plus(
                binding.root.context.getString(
                    R.string.no_of_households_count,
                    profile.houseHoldCount
                )
            )
            binding.root.setOnClickListener {
                selectedItem(profile)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityViewHolder {
        return CommunityViewHolder(
            ItemCommunityProfileBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return communityList.size
    }

    override fun onBindViewHolder(holder: CommunityViewHolder, position: Int) {
        val item = communityList[position]
        holder.bind(item)
    }

}