package com.medtroniclabs.spice.ui.household.summary

import android.content.Context
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.safeClickListener
import com.medtroniclabs.spice.databinding.MembersSummaryListItemBinding
import com.medtroniclabs.spice.db.entity.HouseholdSummaryModel
import com.medtroniclabs.spice.ui.household.HouseholdSelectionListener
class HouseholdSummaryListAdapter(
    private val houseHoldMembersList: ArrayList<HouseholdSummaryModel>
) : RecyclerView.Adapter<HouseholdSummaryListAdapter.HouseholdListViewHolder>() {

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
        }
    }

    private fun getMemberInfoText(context: Context, item: HouseholdSummaryModel): CharSequence {
        return SpannableStringBuilder(
            context.getString(R.string.household_summary_member_info,item.name,item.age, item.gender)
        )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HouseholdListViewHolder {
        return HouseholdListViewHolder(
            MembersSummaryListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return houseHoldMembersList.size
    }

}