package com.medtroniclabs.spice.ui.home.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.RowActivitiesBinding
import com.medtroniclabs.spice.db.entity.MenuEntity
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.home.MenuSelectionListener

class DashboardMenuItemsAdapter(
    private val roleBasedActivitiesList: List<MenuEntity>,
    private val listener: MenuSelectionListener,
) :
    RecyclerView.Adapter<DashboardMenuItemsAdapter.ActivitiesViewHolder>() {
    class ActivitiesViewHolder(val binding: RowActivitiesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ActivitiesViewHolder =
        ActivitiesViewHolder(
            RowActivitiesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: ActivitiesViewHolder,
        position: Int,
    ) {
        val model = roleBasedActivitiesList[position]
        holder.binding.tvTitle.text = if (CommonUtils.parseUserLocale() == DefinedParams.EN) {
            model.name
        } else {
            model.displayValue ?: model.name
        }

        val imageModel = getResourceActivityId(
            model.menuId,
            holder.context,
            model.name,
        )
        val backgroundColorResId = if (model.isDisabled) R.color.border_gray else R.color.white
        holder.binding.root.apply {
            isEnabled = !model.isDisabled
            setCardBackgroundColor(ContextCompat.getColor(holder.context, backgroundColorResId))
        }
        if (imageModel == null) {
            holder.binding.ivActivity.visibility = View.INVISIBLE
        } else {
            holder.binding.ivActivity.visibility = View.VISIBLE
            holder.binding.ivActivity.setImageDrawable(imageModel)
        }

        holder.binding.root.safeClickListener {
            if (model.menuId == MenuConstants.ICCM_MENU_ID &&
                model.name.equals(
                    MenuConstants.OTHER_SYMPTOMS,
                    true,
                )
            ) {
                listener.onMenuSelected(model.name, model.subModule)
            } else {
                listener.onMenuSelected(model.menuId, model.subModule)
            }
        }
    }

    private fun getResourceActivityId(
        menuID: String,
        context: Context,
        name: String,
    ): Drawable? =
        when (menuID) {
            MenuConstants.CBS_MENU_ID.lowercase() -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_cbs,
            )

            MenuConstants.CBS_MENU_ID.uppercase() -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_cbs,
            )

            MenuConstants.HOUSEHOLD_MENU_ID -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_household,
            )

            MenuConstants.MY_PATIENTS_MENU_ID -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_my_patient,
            )

            MenuConstants.SCREENER_MENU_ID -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_general_screener_tool,
            )

            MenuConstants.ICCM_MENU_ID -> {
                if (name == MenuConstants.OTHER_SYMPTOMS) {
                    ContextCompat.getDrawable(context, R.drawable.ic_general)
                } else {
                    ContextCompat.getDrawable(context, R.drawable.ic_general)
                }
            }

            MenuConstants.TB_MENU_ID.lowercase() -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_tb_tool,
            )

            MenuConstants.TB_MENU_ID -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_tb_tool,
            )
            MenuConstants.NCD_MENU_ID -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_ncd_tool,
            )

            MenuConstants.RMNCH_MENU_ID, MenuConstants.MATERNAL_HEALTH, MenuConstants.PREGNANT_WOMEN_PROFILE -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_rmnch_tool,
            )

            MenuConstants.OTHER_SYMPTOMS -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_general,
            )

            MenuConstants.GENERAL_ID -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_general,
            )

            MenuConstants.MOTHER_AND_NEONATE_ID -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_mother_neonate,
            )

            MenuConstants.UNDER_AGE_FIVE_TO_TWO_MONTHS_ID -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_child_under_5,
            )

            MenuConstants.UNDER_AGE_ABOVE_FIVE_YEAR_ID -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_child_above_5,
            )

            MenuConstants.EPI_ID -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_epi,
            )
            MenuConstants.PerformanceMonitoring_ID -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_performance_monitoring,
            )

            MenuConstants.SCREENING -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_screening,
            )

            MenuConstants.REGISTRATION -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_registration,
            )

            MenuConstants.ASSESSMENT -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_assessment,
            )

            MenuConstants.DASHBOARD -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_dashboard,
            )

            MenuConstants.LIFESTYLE -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_lifestyle,
            )

            MenuConstants.PSYCHOLOGICAL -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_psycological_menu,
            )

            MenuConstants.MENTAL_HEALTH -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_mental_health,
            )

            MenuConstants.DISPENSE -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_dispense,
            )

            MenuConstants.FOLLOW_UP -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_follow_up,
            )

            MenuConstants.INVESTIGATION -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_investigation,
            )

            MenuConstants.COMMUNITY_PROFILE -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_community_profile,
            )

            MenuConstants.FP_MENU_ID -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_family_planning,
            )

            MenuConstants.FP_MENU_MR -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_family_planning,
            )

            MenuConstants.HIV -> ContextCompat.getDrawable(
                context,
                R.drawable.ic_hiv,
            )

            else -> null
        }

    override fun getItemCount(): Int = roleBasedActivitiesList.size
}
