package com.medtroniclabs.spice.ui.home.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.RowActivitiesBinding
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.MenuConstants
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
            holder.context,
            model.name,
        )
        if (imageModel == null) {
            holder.binding.ivActivity.visibility = View.INVISIBLE
        } else {
            holder.binding.ivActivity.visibility = View.VISIBLE
            holder.binding.ivActivity.setImageDrawable(imageModel)
        }

        holder.binding.root.safeClickListener {
            if (model.menuId == MenuConstants.ICCM_MENU_ID && model.name.equals(
                    MenuConstants.OTHER_SYMPTOMS,
                    true
                )
            ) {
                listener.onMenuSelected(model.name, model.subModule)
            }else{
                listener.onMenuSelected(model.menuId,model.subModule)
            }
        }
    }


    private fun getResourceActivityId(menuID: String, context: Context, name: String): Drawable? {
        return when (menuID) {
            MenuConstants.HOUSEHOLD_MENU_ID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_household
            )

            MenuConstants.MY_PATIENTS_MENU_ID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_my_patient
            )

            MenuConstants.SCREENER_MENU_ID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_general_screener_tool
            )

            MenuConstants.ICCM_MENU_ID -> {
                if (name == MenuConstants.OTHER_SYMPTOMS) {
                    return ContextCompat.getDrawable(context, R.drawable.ic_general)
                } else {
                    return ContextCompat.getDrawable(context, R.drawable.ic_iccm_tool)
                }
            }

            MenuConstants.CBS_MENU_ID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_cbs_tool
            )

            MenuConstants.TB_MENU_ID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_tb_tool
            )

            MenuConstants.NCD_MENU_ID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_ncd_tool
            )

            MenuConstants.RMNCH_MENU_ID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_rmnch_tool
            )

            MenuConstants.OTHER_SYMPTOMS -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_general
            )

            MenuConstants.GENERAL_ID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_general
            )

            MenuConstants.MOTHER_AND_NEONATE_ID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_mother_neonate
            )

            MenuConstants.UNDER_AGE_FIVE_TO_TWO_MONTHS_ID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_child_above_5
            )

            MenuConstants.UNDER_AGE_ABOVE_FIVE_YEAR_ID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_child_under_5
            )

            MenuConstants.EPI_ID -> return ContextCompat.getDrawable(
                context,
                R.drawable.ic_epi
            )
            else -> null
        }
    }

    override fun getItemCount(): Int {
        return roleBasedActivitiesList.size
    }
}