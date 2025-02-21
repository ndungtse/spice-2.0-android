package com.medtroniclabs.spice.ui.communityprofile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.data.community.CommunitySummaryListItem
import com.medtroniclabs.spice.databinding.ItemCommunityProfileDetailsBinding
import com.medtroniclabs.spice.databinding.ItemCommunityProfileEmergencyBinding
import com.medtroniclabs.spice.databinding.ItemCommunityProfileEmergencyChildBinding
import com.medtroniclabs.spice.databinding.ItemCommunityProfileHeaderBinding
import com.medtroniclabs.spice.databinding.ItemCommunityProfileOthersBinding
import com.medtroniclabs.spice.mappingkey.CommunityDetails

class CommunitySummaryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_PROFILE = 0
        const val TYPE_OTHER = 1
        const val TYPE_TITLE = 2
        const val TYPE_EMERGENCY = 3
    }

    private val communitySummaryList = ArrayList<CommunitySummaryListItem>()

    fun updateList(list: List<CommunitySummaryListItem>) {
        val oldCount = communitySummaryList.size
        communitySummaryList.clear()
        communitySummaryList.addAll(list)
        notifyItemRangeRemoved(0, oldCount)
        notifyItemRangeChanged(0, list.size)
    }

    override fun getItemViewType(position: Int): Int {
        return when (communitySummaryList[position]) {
            is CommunitySummaryListItem.ProfileItem -> TYPE_PROFILE
            is CommunitySummaryListItem.OtherItem -> TYPE_OTHER
            is CommunitySummaryListItem.TitleItem -> TYPE_TITLE
            is CommunitySummaryListItem.EmergencyItem -> TYPE_EMERGENCY
        }
    }

    class ProfileViewHolder(val binding: ItemCommunityProfileDetailsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommunitySummaryListItem.ProfileItem) {
            binding.tvProfileName.text = item.name
            binding.tvProfileDescription.text = item.desc
            binding.tvRegisteredDate.text = binding.root.context.getString(
                R.string.registered_completed_on,
                item.registeredDate
            )
        }
    }

    class OtherViewHolder(val binding: ItemCommunityProfileOthersBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommunitySummaryListItem.OtherItem) {
            binding.tvLabelName.text = item.label
            binding.tvValue.text = item.value
            if (item.isText) {
                binding.tvValue.visibility = View.VISIBLE
                binding.ivStatus.visibility = View.GONE
            } else {
                if (item.value?.equals(CommunityDetails.True) == true) {
                    binding.ivStatus.setImageResource(R.drawable.ic_tick_blue)
                } else {
                    binding.ivStatus.setImageResource(R.drawable.ic_close_blue)
                }
                binding.ivStatus.visibility = View.VISIBLE
                binding.tvValue.visibility = View.GONE
            }
        }
    }

    class TitleViewHolder(val binding: ItemCommunityProfileHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommunitySummaryListItem.TitleItem) {
            binding.tvTitle.text = item.title
        }
    }

    class EmergencyViewHolder(val binding: ItemCommunityProfileEmergencyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommunitySummaryListItem.EmergencyItem) {
            binding.emergencyParent.removeAllViews()
            binding.tvCHCName.text = item.chcName
            item.valuesMap?.iterator()?.forEach {
                val subView = ItemCommunityProfileEmergencyChildBinding.inflate(
                    LayoutInflater.from(
                        binding.root.context
                    ), binding.emergencyParent, false
                )
                subView.tvEmergencyLabel.text = it.key
                subView.tvEmergencyValue.text = it.value
                binding.emergencyParent.addView(subView.root)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_PROFILE -> ProfileViewHolder(
                ItemCommunityProfileDetailsBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            TYPE_OTHER -> OtherViewHolder(
                ItemCommunityProfileOthersBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            TYPE_TITLE -> TitleViewHolder(
                ItemCommunityProfileHeaderBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            TYPE_EMERGENCY -> EmergencyViewHolder(
                ItemCommunityProfileEmergencyBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return communitySummaryList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = communitySummaryList[position]
        when (item) {
            is CommunitySummaryListItem.ProfileItem -> (holder as ProfileViewHolder).bind(item)
            is CommunitySummaryListItem.OtherItem -> (holder as OtherViewHolder).bind(item)
            is CommunitySummaryListItem.TitleItem -> (holder as TitleViewHolder).bind(item)
            is CommunitySummaryListItem.EmergencyItem -> (holder as EmergencyViewHolder).bind(item)
        }
    }
}