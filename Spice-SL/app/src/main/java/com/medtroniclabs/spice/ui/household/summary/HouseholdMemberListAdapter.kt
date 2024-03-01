package com.medtroniclabs.spice.ui.household.summary

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.safeClickListener
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import com.medtroniclabs.spice.common.DateUtils.calculateAge
import com.medtroniclabs.spice.common.DateUtils.calculateAgeInMonths
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.MembersSummaryListItemBinding
import com.medtroniclabs.spice.db.entity.HouseholdMemberEntity
import com.medtroniclabs.spice.ui.household.MemberSelectionListener
import java.text.SimpleDateFormat
import java.util.Locale

class HouseholdMemberListAdapter(
    private val houseHoldMembersList: ArrayList<HouseholdMemberEntity>,
    private val listener: MemberSelectionListener
) : RecyclerView.Adapter<HouseholdMemberListAdapter.HouseholdListViewHolder>() {

    inner class HouseholdListViewHolder(val binding: MembersSummaryListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onBindViewHolder(
        holder: HouseholdListViewHolder,
        position: Int
    ) {
        val item = houseHoldMembersList[position]
        holder.binding.tvMemberName.text = getMemberInfoText(holder.context, item)
        holder.binding.cardPatient.safeClickListener {
            listener.onMemberSelected(item.id, false)
        }
    }

    private fun getMemberInfoText(context: Context, item: HouseholdMemberEntity): CharSequence {
        return SpannableStringBuilder(
            context.getString(
                R.string.household_summary_member_info,
                item.name,
                getAgeFromDob(item.dateOfBirth, context),
                getGenderText(item.gender, context)
            )
        )
    }

    private fun getGenderText(gender: String, context: Context): String {
        return if (gender.equals(DefinedParams.male, true)) {
            context.getString(R.string.male_prefix)
        } else {
            context.getString(R.string.female_prefix)
        }
    }

    private fun getAgeFromDob(dateOfBirth: String, context: Context): String {
        val ageTriplet = DateUtils.getYearMonthAndDate(
            dateOfBirth, SimpleDateFormat(
                DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                Locale.ENGLISH
            )
        )
        val year = ageTriplet.first
        val age = year?.let { calculateAge(it) }
        return if (age != null && age > 5) {
            "$age"
        } else {
            val startDate = DateUtils.formatStringToDate(
                dateOfBirth, SimpleDateFormat(
                    DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    Locale.ENGLISH
                )
            )
            startDate?.let { date ->
                "${calculateAgeInMonths(date)} ${context.getString(R.string.months)}"
            } ?: kotlin.run {
                return ""
            }
        }
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

}