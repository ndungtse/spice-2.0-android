package com.medtroniclabs.spice.ncd.followup.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.Assessment_Type
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.Defaulters_Type
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.LTFU_Type
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.Referred_Type
import com.medtroniclabs.spice.ncd.followup.fragment.NCDFollowUpSearchFragment

class NCDFollowUpAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        val type = when (position) {
            0 -> Referred_Type
            1 -> Assessment_Type
            2 -> Defaulters_Type
            3 -> LTFU_Type
            else -> ""
        }
        return NCDFollowUpSearchFragment.newInstance(type)
    }
}