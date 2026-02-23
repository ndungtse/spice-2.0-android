package com.medtroniclabs.spice.ui.communityprofile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.data.community.CommunityProfileDetail
import com.medtroniclabs.spice.databinding.ItemCommunityProfileBinding

class CommunityListAdapter(
    private val selectedItem: (CommunityProfileDetail) -> Unit,
) : RecyclerView.Adapter<CommunityListAdapter.CommunityViewHolder>() {
    private val communityList = mutableListOf<CommunityProfileDetail>()

    fun updateList(newCommunityList: List<CommunityProfileDetail>) {
        val oldCount = communityList.size
        communityList.clear()
        communityList.addAll(newCommunityList)
        notifyItemRangeRemoved(0, oldCount)
        notifyItemRangeInserted(0, newCommunityList.size)
    }

    inner class CommunityViewHolder(private val binding: ItemCommunityProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(profile: CommunityProfileDetail) {
            binding.tvCommunityName.text = profile.villageName
            binding.tvNoOfHouseholdCount.text = " ".plus(
                binding.root.context.getString(
                    R.string.no_of_households_count,
                    profile.houseHoldCount,
                ),
            )
            binding.root.setOnClickListener {
                selectedItem(profile)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CommunityViewHolder =
        CommunityViewHolder(
            ItemCommunityProfileBinding.inflate(
                LayoutInflater.from(
                    parent.context,
                ),
                parent,
                false,
            ),
        )

    override fun getItemCount(): Int = communityList.size

    override fun onBindViewHolder(
        holder: CommunityViewHolder,
        position: Int,
    ) {
        val item = communityList[position]
        holder.bind(item)
    }
}
