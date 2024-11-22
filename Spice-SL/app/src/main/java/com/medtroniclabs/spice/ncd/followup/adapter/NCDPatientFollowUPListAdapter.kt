package com.medtroniclabs.spice.ncd.followup.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.databinding.FollowUpListItemPatientsBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.data.PatientFollowUpEntity
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.LTFU_Type
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.SCREENED
import com.medtroniclabs.spice.ncd.followup.NCDFollowUpUtils.getDaysString
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.mypatients.PatientSelectionListenerForFollowUp

class NCDPatientFollowUPListAdapter(
    val listener: PatientSelectionListenerForFollowUp,
    val activity : BaseActivity
) : PagingDataAdapter<PatientFollowUpEntity, NCDPatientFollowUPListAdapter.NCDPatientFollowUPListViewHolder>(
    PatientListComparator
) {

    inner class NCDPatientFollowUPListViewHolder(val binding: FollowUpListItemPatientsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context

        fun bind(item: PatientFollowUpEntity) {
            val context = binding.root.context
            val name = item.name ?: context.getString(R.string.separator_hyphen)
            val gender = item.gender?.lowercase()?.capitalizeFirstChar()
                ?: context.getString(R.string.separator_hyphen)

            val age = item.age?.toString() ?: ""
            val patientInfo = context.getString(
                R.string.household_summary_member_info,
                name,
                age,
                gender
            )
            binding.tvPatientName.text = patientInfo
            binding.tvDueInformation.setTextColor(Color.parseColor("#994242"))
            binding.tvReason.gone()
            binding.tvLabelReasonSeperator.gone()
            binding.tvLabelReason.gone()
            if (item.type.equals(SCREENED, true)) {
                val referredReasonsText =
                    item.referredReasons?.filterNot { it.isBlank() }?.joinToString(", ")
                updateReasonSection(
                    label = context.getString(R.string.reason),
                    text = referredReasonsText,
                    context = context,
                    binding = binding
                )
            }
            if (item.type.equals(LTFU_Type, true)) {
                val overDueCategoriesText = item.overDueCategories
                    ?.asSequence()
                    ?.filter { it.isNotBlank() }
                    ?.joinToString(", ") { it.lowercase().capitalizeFirstChar() }
                updateReasonSection(
                    label = context.getString(R.string.overdue),
                    text = overDueCategoriesText,
                    context = context,
                    binding = binding
                )
            }
            item.referredDateSince?.let {
                binding.tvDueInformation.visible()
                binding.tvDueInformation.text =
                    context.getString(
                        getDaysString(item.referredDateSince),
                        item.referredDateSince
                    )
            } ?: binding.tvDueInformation.gone()

            item.retryAttempts?.let {
                binding.ivRecentAttemptCount.apply {
                    visibility = View.VISIBLE
                    text = it.toString()
                }
            } ?: kotlin.run { binding.ivRecentAttemptCount.visibility = View.GONE }
            binding.callButton.safeClickListener {
                listener.onSelectedPatientForCall(item)
            }
            binding.assessmentButton.safeClickListener {
                listener.onSelectedPatientForAssessment(item)
            }
            binding.root.safeClickListener {
                listener.onSelectedPatientCard(item)
            }
        }
    }

    private fun updateReasonSection(
        label: String,
        text: String?,
        context: Context,
        binding: FollowUpListItemPatientsBinding // Replace with the actual type of your binding
    ) {
        if (!text.isNullOrEmpty()) {
            binding.tvLabelReason.text = label
            binding.tvReason.text = text
            binding.tvReason.visible()
            binding.tvLabelReasonSeperator.visible()
            binding.tvLabelReason.visible()
        } else {
            binding.tvReason.text = context.getString(R.string.hyphen_symbol)
            binding.tvReason.gone()
            binding.tvLabelReasonSeperator.gone()
            binding.tvLabelReason.gone()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NCDPatientFollowUPListViewHolder {
        return NCDPatientFollowUPListViewHolder(
            FollowUpListItemPatientsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: NCDPatientFollowUPListViewHolder, position: Int) {
        getItem(position)?.let { item ->
            holder.bind(item)
        }
    }

    object PatientListComparator : DiffUtil.ItemCallback<PatientFollowUpEntity>() {
        override fun areItemsTheSame(
            oldItem: PatientFollowUpEntity,
            newItem: PatientFollowUpEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PatientFollowUpEntity,
            newItem: PatientFollowUpEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }

    fun getDrawable(view: ConstraintLayout, colorCode: String) {
        if (view.background != null) {
            val drawable = view.background as GradientDrawable
            drawable.mutate()
            drawable.setStroke(3, Color.parseColor(colorCode))
        }
    }
}