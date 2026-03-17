package org.medtroniclabs.uhis.ui.medicalreview.hiv.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.toCleanString
import org.medtroniclabs.uhis.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import org.medtroniclabs.uhis.common.DateUtils.DATE_ddMMyyyy
import org.medtroniclabs.uhis.common.DateUtils.convertDateFormat
import org.medtroniclabs.uhis.data.resource.CD4DetailsResponse
import org.medtroniclabs.uhis.databinding.ItemCd4RowBinding

class CD4Adapter(private val cd4List: List<CD4DetailsResponse>, val isCD4: Boolean = false, val isCD4Percentage: Boolean = false) :
    RecyclerView.Adapter<CD4Adapter.CD4ViewHolder>() {
    class CD4ViewHolder(val binding: ItemCd4RowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            response: CD4DetailsResponse,
            cd4List: List<CD4DetailsResponse>,
            isCD4: Boolean,
            isCD4Percentage: Boolean,
        ) {
            binding.tvDate.text = response.dateValue?.let {
                convertDateFormat(
                    it,
                    DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    DATE_ddMMyyyy,
                )
            } ?: binding.root.context.getString(R.string.separator_double_hyphen)
            binding.tvCd4Percentage.text = getCD4Value(response, isCD4Percentage)
            if (bindingAdapterPosition == cd4List.size - 1) {
                binding.divider.visibility = View.GONE
            } else {
                binding.divider.visibility = View.VISIBLE
            }
        }

        private fun getCD4Value(
            response: CD4DetailsResponse,
            isCD4Percentage: Boolean,
        ): String =
            if (isCD4Percentage) {
                response.cd4Percentage?.toCleanString()
            } else {
                response.cd4?.toCleanString()
            } ?: binding.root.context.getString(R.string.hyphen_symbol)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CD4ViewHolder =
        CD4ViewHolder(
            ItemCd4RowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: CD4ViewHolder,
        position: Int,
    ) = holder.bind(cd4List[position], cd4List, isCD4, isCD4Percentage)

    override fun getItemCount(): Int = cd4List.size
}
