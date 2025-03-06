package com.medtroniclabs.spice.ui.household.summary

import android.content.Context
import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.getAgeFromDOB
import com.medtroniclabs.spice.common.CommonUtils.getGenderText
import com.medtroniclabs.spice.databinding.MembersSummaryListItemBinding
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.household.MemberSelectionListener

class HouseholdMemberListAdapter(
    private val houseHoldMembersList: List<HouseholdMemberEntity>,
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
        position: Int
    ) {
        val context = holder.context
        val item = houseHoldMembersList[position]
        val tbStatus = houseHoldMembersList[position].tBContactTraceStatus
        holder.binding.clReasonOfDeath.gone()
        holder.binding.forwardIcon.visible()
        //Null -> TB Negative  1-> TB Positive  2-> contactTrace need to do 3 -> ContactTracing done
        if (tbStatus != null) {
            when (tbStatus) {
                1 -> {
                    holder.binding.tvDiagnosisStatus.text =
                        context.resources.getString(R.string.trug_sensitive_tb)
                    holder.binding.tvContactTracingStatus.gone()
                    holder.binding.groupViewContactTrace.visible()
                }

                2 -> {
                    holder.binding.tvDiagnosisStatus.text =
                        context.resources.getString(R.string.separator_double_hyphen)
                    holder.binding.tvDiagnosisStatus.setTextColor(
                        ColorStateList.valueOf(context.getColor(R.color.grey_black))
                    )

                    holder.binding.tvContactTracingStatus.text =
                        context.resources.getString(R.string.update_contact_tracing)
                    holder.binding.tvContactTracingStatus.setTextColor(
                        ColorStateList.valueOf(context.getColor(R.color.card_color))
                    )
                    holder.binding.tvContactTracingStatus.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_alert_red),
                        null,
                        null,
                        null
                    )
                    holder.binding.tvContactTracingStatus.visible()
                    holder.binding.groupViewContactTrace.visible()
                    holder.binding.tvContactTracingStatus.safeClickListener(this)
                    holder.binding.tvContactTracingStatus.tag = position
                }

                3 -> {
                    holder.binding.tvDiagnosisStatus.text =
                        context.resources.getString(R.string.separator_double_hyphen)
                    holder.binding.tvDiagnosisStatus.setTextColor(
                        ColorStateList.valueOf(context.getColor(R.color.grey_black))
                    )

                    holder.binding.tvContactTracingStatus.text =
                        context.resources.getString(R.string.contact_tracing_updated)
                    holder.binding.tvContactTracingStatus.setTextColor(
                        ColorStateList.valueOf(context.getColor(R.color.secondary_green_color))
                    )
                    holder.binding.tvContactTracingStatus.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(context, R.drawable.ic_circle_tick_green),
                        null,
                        null,
                        null
                    )
                    holder.binding.tvContactTracingStatus.visible()
                    holder.binding.groupViewContactTrace.visible()
                }

                else -> {
                    holder.binding.groupViewContactTrace.gone()
                    holder.binding.tvContactTracingStatus.gone()
                }
            }

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
        if(!phuWalkInsFlow) {
        holder.binding.cardPatient.safeClickListener {
            if (item.isActive) {
                listener.onMemberSelected(item.id, false, item.dateOfBirth)
            }
        }
        } else {
            holder.binding.forwardIcon.gone()

        }
    }

    private fun disableAllChildren(root: MaterialCardView, alpha: Float, enabled: Boolean) {
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            child.alpha = alpha // Set 50% opacity to indicate a disabled state
            child.isEnabled = enabled // Optionally disable interaction
        }
    }

    private fun getMemberInfoText(context: Context, item: HouseholdMemberEntity): CharSequence {
        return SpannableStringBuilder(
            context.getString(
                R.string.household_summary_member_info,
                item.name,
                getAgeFromDOB(item.dateOfBirth, context),
                getGenderText(item.gender, context)
            )
        )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HouseholdListViewHolder {
        return HouseholdListViewHolder(
            MembersSummaryListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return houseHoldMembersList.size
    }

    override fun onClick(view: View?) {
        when (view?.id){
            R.id.tvContactTracingStatus -> {
                val pos = view.tag as Int
                listener.onMemberSelected(houseHoldMembersList[pos].id, true, houseHoldMembersList[pos].dateOfBirth,true)
            }
        }
    }

}