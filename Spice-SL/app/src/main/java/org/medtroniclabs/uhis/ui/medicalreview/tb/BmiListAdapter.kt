package org.medtroniclabs.uhis.ui.medicalreview.tb

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.data.model.BpAndWeightResponse
import org.medtroniclabs.uhis.databinding.ListItemBmiLogBinding
import org.medtroniclabs.uhis.ui.medicalreview.motherneonate.anc.MotherNeonateUtil

class BmiListAdapter(private var bmiLogs: ArrayList<BpAndWeightResponse> = arrayListOf()) :
    RecyclerView.Adapter<BmiListAdapter.BmiListViewHolder>() {
    inner class BmiListViewHolder(val binding: ListItemBmiLogBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context

        fun bind(bpAndWeightResponse: BpAndWeightResponse) {
            with(binding) {
                tvDate.text = bpAndWeightResponse.dateValue?.takeIf { !it.isNullOrBlank() }?.let {
                    DateUtils.convertDateTimeToDate(
                        it,
                        inputFormat = DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        outputFormat = DateUtils.DATE_ddMMyyyy,
                    )
                } ?: context.getString(R.string.hyphen_symbol)
                tvHeight.text = MotherNeonateUtil.convertHeight(bpAndWeightResponse.height, context)
                tvWeight.text = MotherNeonateUtil.convertWeight(bpAndWeightResponse.weight, context)
                tvBmi.text = MotherNeonateUtil.convertBmi(bpAndWeightResponse.bmi, context)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BmiListViewHolder =
        BmiListViewHolder(
            ListItemBmiLogBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )

    fun setData(data: ArrayList<BpAndWeightResponse>) {
        val oldCount = this.bmiLogs.size
        this.bmiLogs.clear()
        this.bmiLogs = data
        notifyItemRangeRemoved(0, oldCount)
        notifyItemRangeInserted(0, bmiLogs.size)
    }

    override fun onBindViewHolder(
        holder: BmiListViewHolder,
        position: Int,
    ) {
        holder.bind(bmiLogs[position])
    }

    override fun getItemCount(): Int = bmiLogs.size
}
