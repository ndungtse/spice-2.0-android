package org.medtroniclabs.uhis.ncd.assessment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DateUtils.DATE_FORMAT_ddMMMyyyy
import org.medtroniclabs.uhis.databinding.ListItemBpLogBinding
import org.medtroniclabs.uhis.ncd.data.BPLogList

class BPLogListAdapter(private val bpLogs: ArrayList<BPLogList>) :
    RecyclerView.Adapter<BPLogListAdapter.BPLogListViewHolder>() {
    inner class BPLogListViewHolder(val binding: ListItemBpLogBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BPLogListViewHolder =
        BPLogListViewHolder(
            ListItemBpLogBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )

    override fun onBindViewHolder(
        holder: BPLogListViewHolder,
        position: Int,
    ) {
        val bpLog = bpLogs[position]

        bpLog.bpTakenOn?.let {
            val date = DateUtils.convertDateFormat(
                it,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                DATE_FORMAT_ddMMMyyyy,
            )
            holder.binding.tvAssessmentDate.text = date
        }

        val value = "${bpLog.avgSystolic} / ${bpLog.avgDiastolic}"
        holder.binding.tvSystolicDiastolic.text = value
    }

    override fun getItemCount(): Int = bpLogs.size
}
