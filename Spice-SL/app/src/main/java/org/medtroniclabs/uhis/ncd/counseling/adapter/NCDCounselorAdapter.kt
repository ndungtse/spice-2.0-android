package org.medtroniclabs.uhis.ncd.counseling.adapter

import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.textOrEmpty
import org.medtroniclabs.uhis.appextensions.textOrHyphen
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.DateUtils
import org.medtroniclabs.uhis.databinding.ListItemCounselorBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ncd.counseling.utils.ValidationListener
import org.medtroniclabs.uhis.ncd.data.NCDCounselingModel

class NCDCounselorAdapter(private val listener: ValidationListener) :
    RecyclerView.Adapter<NCDCounselorAdapter.ViewHolder>() {
    private var adapterList = ArrayList<NCDCounselingModel>()

    inner class ViewHolder(val binding: ListItemCounselorBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val context: Context = binding.root.context

        fun bind(item: NCDCounselingModel) {
            with(item) {
                binding.apply {
                    if (isExpanded) {
                        resultsLayout.visible()
                        rotateArrow180f(ivDropDown)
                    } else {
                        resultsLayout.gone()
                        rotateArrow0f(ivDropDown)
                    }

                    tvClinicalNote.text = clinicianNote.textOrHyphen()

                    val refDate = referredDate?.let {
                        DateUtils.convertDateTimeToDate(
                            it,
                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                            DateUtils.DATE_FORMAT_ddMMMyyyy,
                        )
                    }
                    tvRefDate.text = refDate.textOrHyphen()

                    tvRefBy.text = referredByDisplay.textOrHyphen()

                    etOtherNotes.setText(counselorAssessment.textOrEmpty())
                    etOtherNotes.addTextChangedListener { value ->
                        adapterList[layoutPosition].apply {
                            this.counselorAssessment = if (value.isNullOrBlank()) {
                                null
                            } else {
                                value.toString()
                            }
                        }
                        listener.validate()
                    }

                    root.safeClickListener(this@ViewHolder)
                }
            }
        }

        override fun onClick(mView: View?) {
            when (mView?.id) {
                binding.root.id -> {
                    if (layoutPosition < adapterList.size) {
                        val item = adapterList[layoutPosition]
                        if (item.id.isNullOrBlank()) {
                            return
                        } else {
                            adapterList[layoutPosition].let {
                                it.isExpanded = !it.isExpanded
                            }
                            notifyItemChanged(layoutPosition)
                        }
                    }
                }
            }
        }
    }

    private fun rotateArrow180f(view: View) {
        val ivArrow = ObjectAnimator.ofFloat(view, "rotation", 0f, 180f)
        ivArrow.start()
    }

    private fun rotateArrow0f(view: View) {
        val ivArrow = ObjectAnimator.ofFloat(view, "rotation", 180f, 0f)
        ivArrow.start()
    }

    private fun getTextColor(
        context: Context,
        enteredBy: Any?,
    ): Int =
        if (enteredBy == null) {
            context.getColor(R.color.disabled_text_color)
        } else {
            context.getColor(
                R.color.navy_blue,
            )
        }

    fun getData() = adapterList

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder =
        ViewHolder(
            ListItemCounselorBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        adapterList.let {
            holder.bind(it[position])
        }
    }

    override fun getItemCount(): Int = adapterList.size

    fun submitData(list: ArrayList<NCDCounselingModel>) {
        adapterList = ArrayList(list)
        notifyItemRangeChanged(0, adapterList.size)
    }
}
