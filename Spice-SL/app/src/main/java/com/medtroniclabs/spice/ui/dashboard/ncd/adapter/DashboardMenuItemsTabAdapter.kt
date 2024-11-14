package com.medtroniclabs.spice.ui.dashboard.ncd.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.HomeRowActivityBinding
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.ui.MenuConstants

class DashboardMenuItemsTabAdapter(
    private var roleBasedActivitiesList: ArrayList<MenuEntity> = arrayListOf(),
) :
    RecyclerView.Adapter<DashboardMenuItemsTabAdapter.ActivitiesViewHolder>() {

    class ActivitiesViewHolder(val binding: HomeRowActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(menuEntity: MenuEntity) {
            binding.tvTitle.text = menuEntity.name
            binding.tvCount.text = menuEntity.patientCount.toString()
            val imageModel = getResourceActivityId(
                menuEntity.menuId,
                context
            )
            if (imageModel == null) {
                binding.ivActivity.visibility = View.INVISIBLE
            } else {
                binding.ivActivity.visibility = View.VISIBLE
                binding.ivActivity.setImageDrawable(imageModel)
            }
        }

        private fun getResourceActivityId(
            menuId: String,
            context: Context
        ): Drawable? {
            return when (menuId) {
                MenuConstants.SCREENING_CONDUCTED -> return ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_group_search
                )

                MenuConstants.ASSESSMENT_CONDUCTED, MenuConstants.COUNSELLINGS_CONDUCTED, MenuConstants.INVESTIGATIONS_CONDUCTED -> return ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_group_assessment
                )

                MenuConstants.REGISTRATION_CONDUCTED -> return ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_group_registration
                )

                MenuConstants.PRESCRIPTIONS_DISPENSED -> return ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_group_prescription
                )

                MenuConstants.REVIEWS_CONDUCTED -> return ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_group_review
                )

                MenuConstants.NO_OF_REFERRALS -> return ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_group_assessment
                )

                else -> null
            }
        }

        val context: Context = binding.root.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivitiesViewHolder {
        return ActivitiesViewHolder(
            HomeRowActivityBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ActivitiesViewHolder, position: Int) {
        roleBasedActivitiesList[position].let { holder.bind(it) }
    }

    fun updateData(data: ArrayList<MenuEntity>) {
        roleBasedActivitiesList.clear()
        roleBasedActivitiesList = data
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return roleBasedActivitiesList.size
    }
}

