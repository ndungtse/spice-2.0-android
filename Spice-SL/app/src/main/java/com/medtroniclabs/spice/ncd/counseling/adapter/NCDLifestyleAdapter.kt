package com.medtroniclabs.spice.ncd.counseling.adapter

import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.databinding.AdapterNcdLifestyleBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ncd.counseling.model.NCDCounselingModel
import com.medtroniclabs.spice.ncd.counseling.utils.CounselingInterface

class NCDLifestyleAdapter(private val lifeStyleInterface: CounselingInterface) :
    RecyclerView.Adapter<NCDLifestyleAdapter.ViewHolder>() {
    private var adapterList = ArrayList<NCDCounselingModel>()

    inner class ViewHolder(val binding: AdapterNcdLifestyleBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val context: Context = binding.root.context

        fun bind(item: NCDCounselingModel) {
            with(item) {
                binding.apply {
                    if (isExpanded) {
                        resultsLayout.visibility = View.VISIBLE
                        rotateArrow180f(ivDropDown)
                    } else {
                        resultsLayout.visibility = View.GONE
                        rotateArrow0f(ivDropDown)
                    }

                    clinicianNotesLayout.apply {
                        tvKey.text = context.getString(R.string.clinician_notes)
                        tvValue.text = clinicianNote ?: run { displayHyphen(context) }
                    }

                    if (assessedBy.isNullOrBlank()) {
                        assessmentNotesLayout.root.visibility = View.GONE
                        otherNotesLayout.root.visibility = View.GONE
                    } else {
                        assessmentNotesLayout.apply {
                            root.visibility = View.VISIBLE
                            tvKey.text = context.getString(R.string.lifestyle_assessment)
                            tvValue.text = lifestyleAssessment ?: run { displayHyphen(context) }
                        }

                        otherNotesLayout.apply {
                            root.visibility = View.VISIBLE
                            tvKey.text = context.getString(R.string.other_notes)
                            tvValue.text = otherNote ?: run { displayHyphen(context) }
                        }
                    }

                    tvReferredFor.text =
                        lifestyles?.joinToString(separator = ", ") ?: run { displayHyphen(context) }

                    tvRefDate.text = referredDate?.let {
                        DateUtils.convertDateTimeToDate(
                            it,
                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                            DateUtils.DATE_FORMAT_ddMMMyyyy
                        )
                    } ?: run {
                        displayHyphen(context)
                    }

                    tvRefBy.text = referredBy ?: run { displayHyphen(context) }

                    tvAssessedDate.apply {
                        text = assessedDate?.let {
                            DateUtils.convertDateTimeToDate(
                                it,
                                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                                DateUtils.DATE_FORMAT_ddMMMyyyy
                            )
                        } ?: run {
                            context.getString(R.string.not_assessed)
                        }
                        setTextColor(getTextColor(context, assessedDate))
                    }

                    tvAssessedBy.apply {
                        text = assessedBy ?: run { context.getString(R.string.not_available) }
                        setTextColor(getTextColor(context, assessedBy))
                    }

                    if (id.isNullOrBlank()) {
                        ivDropDown.visibility = View.INVISIBLE
                        ivRemove.visibility =
                            if (assessedBy.isNullOrBlank()) View.VISIBLE else View.GONE
                        ivDelete.visibility =
                            if (assessedBy.isNullOrBlank()) View.GONE else View.VISIBLE
                    } else {
                        ivDropDown.visibility = View.VISIBLE
                        ivRemove.visibility = View.GONE
                        ivDelete.visibility = View.GONE
                    }

                    ivRemove.safeClickListener(this@ViewHolder)
                    ivDelete.safeClickListener(this@ViewHolder)
                    root.safeClickListener(this@ViewHolder)
                }
            }
        }

        override fun onClick(mView: View?) {
            when (mView?.id) {
                binding.ivRemove.id, binding.ivDelete.id -> {
                    if (layoutPosition < adapterList.size) {
                        adapterList[layoutPosition].let { item ->
                            if (item.id == null)
                                lifeStyleInterface.removeElement(item)
                            else
                                showMessage(context, item)
                        }
                    }
                }

                binding.root.id -> {
                    if (layoutPosition < adapterList.size) {
                        val item = adapterList[layoutPosition]
                        if (item.id.isNullOrBlank())
                            return
                        else {
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

    private fun displayHyphen(context: Context): String {
        return context.getString(R.string.hyphen_symbol)
    }

    private fun showMessage(context: Context, item: NCDCounselingModel) {
        if ((context as BaseActivity).connectivityManager.isNetworkAvailable()) {
            context.showErrorDialogue(
                title = context.getString(R.string.confirmation),
                message = context.getString(R.string.delete_confirmation),
                isNegativeButtonNeed = true,
                positiveButtonName = context.getString(R.string.yes)
            ) { isPositiveResult ->
                if (isPositiveResult)
                    lifeStyleInterface.removeElement(item)
            }
        } else {
            context.showErrorDialogue(
                context.getString(R.string.error),
                context.getString(R.string.no_internet_error),
                false,
            ) {}
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

    private fun getTextColor(context: Context, enteredBy: Any?): Int {
        return if (enteredBy == null) context.getColor(R.color.disabled_text_color) else context.getColor(
            R.color.navy_blue
        )
    }

    fun getData() = adapterList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AdapterNcdLifestyleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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