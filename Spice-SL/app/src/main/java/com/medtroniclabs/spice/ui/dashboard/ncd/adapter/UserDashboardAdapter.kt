package com.medtroniclabs.spice.ui.dashboard.ncd.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.databinding.ListItemUserDashboardBinding

class UserDashboardAdapter(private val userDashboardLists: ArrayList<Triple<String, Int, Int>>) :
    RecyclerView.Adapter<UserDashboardAdapter.UserDashboardViewHolder>() {

    inner class UserDashboardViewHolder(val binding: ListItemUserDashboardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserDashboardViewHolder {
        return UserDashboardViewHolder(
            ListItemUserDashboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: UserDashboardViewHolder,
        position: Int
    ) {
        userDashboardLists[position].let { dashboard ->
            holder.binding.apply {
                tvKey.text = dashboard.first
                tvValue.text = dashboard.second.toString()
                ivActivity.setImageDrawable(
                    ContextCompat.getDrawable(
                        root.context,
                        dashboard.third
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return userDashboardLists.size
    }
}

