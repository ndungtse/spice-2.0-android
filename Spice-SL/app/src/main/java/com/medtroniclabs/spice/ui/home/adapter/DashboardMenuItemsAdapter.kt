package com.medtroniclabs.spice.ui.home.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.safeClickListener
import com.medtroniclabs.spice.databinding.RowActivitiesBinding
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.ui.ItemConstants
import com.medtroniclabs.spice.ui.home.MenuSelectionListener

class DashboardMenuItemsAdapter(
    private val roleBasedActivitiesList: List<MenuEntity>,
    private val listener: MenuSelectionListener
) :
    RecyclerView.Adapter<DashboardMenuItemsAdapter.ActivitiesViewHolder>() {

    class ActivitiesViewHolder(val binding: RowActivitiesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivitiesViewHolder {
        return ActivitiesViewHolder(
            RowActivitiesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ActivitiesViewHolder, position: Int) {
        val model = roleBasedActivitiesList[position]
        holder.binding.tvTitle.text = model.name

        val imageModel = getResourceActivityId(
            model.menuId,
            holder.context
        )
        if (imageModel == null){
            holder.binding.ivActivity.visibility = View.INVISIBLE
        }else{
            holder.binding.ivActivity.visibility = View.VISIBLE
            holder.binding.ivActivity.setImageDrawable(imageModel)
        }

        holder.binding.root.safeClickListener {
            listener.onMenuSelected(model.menuId)
        }
    }


    private fun getResourceActivityId(model: String, context: Context): Drawable? {
        return when (model) {
            ItemConstants.HOUSEHOLD_MENU_ID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_household
            )
            ItemConstants.MY_PATIENTS_MENU_ID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_my_patient
            )
            else -> null
        }
    }

    override fun getItemCount(): Int {
        return roleBasedActivitiesList.size
    }
}