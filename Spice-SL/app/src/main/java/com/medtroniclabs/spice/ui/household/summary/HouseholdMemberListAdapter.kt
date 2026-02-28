package com.medtroniclabs.spice.ui.household.summary

import android.content.Context
import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.getAgeFromDOB
import com.medtroniclabs.spice.common.CommonUtils.getGenderText
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.data.offlinesync.model.HouseholdMemberWithTb
import com.medtroniclabs.spice.databinding.MembersSummaryListItemBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.household.MemberSelectionListener

class HouseholdMemberListAdapter(
    private val houseHoldMembersList: List<HouseholdMemberWithTb>,
    private val listener: MemberSelectionListener,
    private val phuWalkInsFlow: Boolean,
) : RecyclerView.Adapter<HouseholdMemberListAdapter.HouseholdListViewHolder>(),
    View.OnClickListener {
    inner class HouseholdListViewHolder(val binding: MembersSummaryListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onBindViewHolder(
        holder: HouseholdListViewHolder,
        position: Int,
    ) {
        val context = holder.context
        val item = houseHoldMembersList[position]

        holder.binding.clReasonOfDeath.gone()
        holder.binding.forwardIcon.visible()

        if (phuWalkInsFlow) {
            if (item.diagnoses != null && item.diagnoses.trim().isNotEmpty()) {
                holder.binding.tvDiagnosisStatus.text = item.diagnoses
                holder.binding.tvDiagnosisStatus.setTextColor(
                    ColorStateList.valueOf(context.getColor(R.color.a_red_error)),
                )
            } else {
                holder.binding.tvDiagnosisStatus.text =
                    context.resources.getString(R.string.separator_double_hyphen)
                holder.binding.tvDiagnosisStatus.setTextColor(
                    ColorStateList.valueOf(context.getColor(R.color.grey_black)),
                )
            }
        } else {
            holder.binding.tvDiagnosis.gone()
            holder.binding.tvDiagnosisSeparator.gone()
            holder.binding.tvDiagnosisSeparator.gone()
            holder.binding.tvRecentService.visible()
            holder.binding.tvRecentServiceSeparator.visible()
            holder.binding.tvRecentServiceValue.visible()
            holder.binding.tvRecentServiceDate.visible()
            holder.binding.tvRecentServiceDateSeparator.visible()
            holder.binding.tvRecentServiceDateValue.visible()

            holder.binding.tvRecentServiceValue.text = item.recentService ?: context.resources.getString(R.string.separator_double_hyphen)
            holder.binding.tvRecentServiceDateValue.text = item.recentServiceDate?.let {
                DateUtils.formatDateToDisplayFormat(it)
            } ?: context.resources.getString(R.string.separator_double_hyphen)
        }

        if (item.isActive) {
            holder.binding.tvMemberName.text = getMemberInfoText(context, item)
            holder.binding.clPatientRoot.setBackgroundResource(R.drawable.default_color_bg)
            disableAllChildren(holder.binding.root, 1f, true)
        } else {
            holder.binding.tvMemberName.text =
                "${getMemberInfoText(context, item)} (${context.getString(R.string.deceased)})"
            holder.binding.clPatientRoot.setBackgroundResource(R.drawable.drak_grey_bg)
            holder.binding.clReasonOfDeath.visible()
            holder.binding.forwardIcon.invisible()
            holder.binding.tvReasonForDeath.text =
                item.deceasedReason ?: context.getString(R.string.separator_double_hyphen)
            disableAllChildren(holder.binding.root, 1f, false)
        }

        holder.binding.tvPatientId.text = item.patientId ?: context.getString(R.string.separator_double_hyphen)
        if (!phuWalkInsFlow) {
            holder.binding.cardPatient.safeClickListener {
                if (item.isActive) {
                    listener.onMemberSelected(
                        item.id,
                        false,
                        item.dateOfBirth,
                        houseHoldId = item.householdId,
                    )
                }
            }
        } else {
            holder.binding.forwardIcon.gone()
        }
    }

    private fun disableAllChildren(
        root: MaterialCardView,
        alpha: Float,
        enabled: Boolean,
    ) {
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            child.alpha = alpha // Set 50% opacity to indicate a disabled state
            child.isEnabled = enabled // Optionally disable interaction
        }
    }

    private fun getMemberInfoText(
        context: Context,
        item: HouseholdMemberWithTb,
    ): CharSequence =
        SpannableStringBuilder(
            context.getString(
                R.string.household_summary_member_info,
                item.name,
                getAgeFromDOB(item.dateOfBirth, context),
                getGenderText(item.gender, context),
            ),
        )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HouseholdListViewHolder =
        HouseholdListViewHolder(
            MembersSummaryListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun getItemCount(): Int = houseHoldMembersList.size

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvContactTracingStatus -> {
                val pos = view.tag as Int
                listener.onMemberSelected(
                    houseHoldMembersList[pos].id,
                    true,
                    houseHoldMembersList[pos].dateOfBirth,
                    true,
                )
            }
        }
    }
}
