package org.medtroniclabs.uhis.ui.services

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.invisible
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.CommonUtils.getAgeFromDOB
import org.medtroniclabs.uhis.common.CommonUtils.getGenderText
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.data.offlinesync.model.HouseholdMemberWithTb
import org.medtroniclabs.uhis.databinding.MembersSummaryListItemBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ui.assessment.utils.AssessmentUtil
import org.medtroniclabs.uhis.ui.household.MemberSelectionListener

class ServiceMembersAdapter(
    private val listener: MemberSelectionListener,
) : RecyclerView.Adapter<ServiceMembersAdapter.HouseholdListViewHolder>() {
    private val membersList = mutableListOf<HouseholdMemberWithTb>()

    fun setMembersList(list: List<HouseholdMemberWithTb>) {
        val oldCount = membersList.size
        membersList.clear()
        membersList.addAll(list)
        notifyItemRangeRemoved(0, oldCount)
        notifyItemRangeInserted(0, list.size)
    }

    class HouseholdListViewHolder(val binding: MembersSummaryListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onBindViewHolder(
        holder: HouseholdListViewHolder,
        position: Int,
    ) {
        val context = holder.context
        val item = membersList[position]

        holder.binding.clReasonOfDeath.gone()
        holder.binding.forwardIcon.visible()

        holder.binding.tvRecentService.visible()
        holder.binding.tvRecentServiceSeparator.visible()
        holder.binding.tvRecentServiceValue.visible()
        holder.binding.tvRecentServiceDate.visible()
        holder.binding.tvRecentServiceDateSeparator.visible()
        holder.binding.tvRecentServiceDateValue.visible()

        holder.binding.tvRecentServiceValue.text = item.recentService?.let { recentService -> AssessmentUtil.mapServiceToServiceName(recentService) }
            ?: context.resources.getString(R.string.separator_double_hyphen)
        holder.binding.tvRecentServiceDateValue.text = item.recentServiceDate?.let {
            DateUtils.formatDateToDisplayFormat(it)
        } ?: context.resources.getString(R.string.separator_double_hyphen)

        holder.binding.tvDiagnosis.setText(R.string.ss_name)
        holder.binding.tvDiagnosisStatus.setTextColor(ContextCompat.getColor(holder.binding.root.context, R.color.grey_black))
        val shasthyaShebika = when {
            !item.shasthyaShebikaNameSsId.isNullOrBlank() && !item.shasthyaShebikaName.isNullOrBlank() -> {
                "${item.shasthyaShebikaNameSsId} - ${item.shasthyaShebikaName}"
            }
            !item.shasthyaShebikaName.isNullOrBlank() -> {
                item.shasthyaShebikaName
            }
            else -> {
                context.getString(R.string.separator_double_hyphen)
            }
        }
        holder.binding.tvDiagnosisStatus.text = shasthyaShebika

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

    override fun getItemCount(): Int = membersList.size
}
