package org.medtroniclabs.uhis.ui.household.adapter

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.db.entity.MemberAssessmentHistoryEntity
import org.medtroniclabs.uhis.formgeneration.extension.px
import org.medtroniclabs.uhis.ui.assessment.utils.AssessmentUtil

/**
 * Adapter for showing member assessment history
 */
class DateSelectionAdapter(
    private val historyList: List<MemberAssessmentHistoryEntity>,
    private val onItemClickListener: (Int) -> Unit,
) : RecyclerView.Adapter<DateSelectionAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        itemType: Int,
    ): ViewHolder =
        ViewHolder(
            TextView(viewGroup.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                setPadding(8.px)
            },
        )

    override fun onBindViewHolder(
        viewHolder: ViewHolder,
        position: Int,
    ) {
        viewHolder.bindData(historyList[position])
        viewHolder.view.setOnClickListener {
            onItemClickListener(viewHolder.bindingAdapterPosition)
        }
    }

    override fun getItemCount() = historyList.size

    class ViewHolder(val view: TextView) : RecyclerView.ViewHolder(view) {
        fun bindData(history: MemberAssessmentHistoryEntity) {
            val visitDateMillis = DateUtils.getLastMenstrualDate(history.visitDate ?: "").timeInMillis
            val displayDate = DateUtils.formatDateToDisplayFormat(visitDateMillis)
            val dataToDisplay = "$displayDate - ${AssessmentUtil.mapServiceToServiceName(history.serviceProvided ?: "", view.context)}"
            view.text = dataToDisplay
        }
    }
}
