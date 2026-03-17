package org.medtroniclabs.uhis.ui.mypatients.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.common.DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
import org.medtroniclabs.uhis.common.DateUtils.DATE_ddMMyyyy
import org.medtroniclabs.uhis.databinding.LayoutDateListAdapterBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.model.ReferredDate

class DateListAdapter(
    val list: ArrayList<ReferredDate> = arrayListOf(),
    var id: String? = null,
    val onClick: (ReferredDate) -> Unit,
) :
    RecyclerView.Adapter<DateListAdapter.DateViewHolder>() {
    inner class DateViewHolder(val binding: LayoutDateListAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReferredDate) {
            with(binding) {
                tvDate.text = item.date?.let {
                    DateUtils.convertDateFormat(
                        it,
                        DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DATE_ddMMyyyy,
                    )
                }
                ivSelected.visibility = if (id == item.id) View.VISIBLE else View.INVISIBLE
                root.safeClickListener {
                    onClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): DateViewHolder =
        DateViewHolder(
            LayoutDateListAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: DateViewHolder,
        position: Int,
    ) {
        holder.bind(list[position])
    }

    fun submitData(
        referredDates: List<ReferredDate>,
        id: String?,
    ) {
        this.list.clear()
        this.list.addAll(referredDates)
        this.id = id
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size
}
