package com.medtroniclabs.spice.ui.medicalreview.pharmacist

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.databinding.LayoutPrescriptionRefillAdapterBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil

class PrescriptionRefillAdapter :
    RecyclerView.Adapter<PrescriptionRefillAdapter.PrescriptionRefillViewHolder>() {

    var list = ArrayList<FillPrescriptionListResponse>()

    inner class PrescriptionRefillViewHolder(val binding: LayoutPrescriptionRefillAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
            private val rootContext: Context = binding.root.context
        fun bind(pos: Int, model: FillPrescriptionListResponse) {
            val hypen = rootContext.getString(R.string.hyphen_symbol)
            binding.tvMedicationName.text = model.medicationName.ifBlank { hypen }
            binding.tvDosage.text =
                getDosageValue(model.dosageUnitValue, model.dosageUnitName)
            binding.tvFrequency.text = model.dosageFrequencyName
            binding.tvMedicationPrescribedDays.text = model.remainingPrescriptionDays.toString()
            binding.ivFormType.setImageDrawable(
                getFormDosage(
                    model.dosageFormName,
                    rootContext
                )
            )
            binding.ivDosageFrom.setImageDrawable(
                getFormDosage(
                    model.dosageFormName,
                    rootContext
                )
            )
            model.prescriptionFilledDays?.let {
                binding.tvDaysFilled.setText(it)
            }

            binding.tvDaysFilled.addTextChangedListener { editable ->
                list[layoutPosition].let {
                    if (editable.isNullOrBlank()) {
                        it.prescriptionFilledDays = null
                    } else {
                        it.prescriptionFilledDays = editable.toString().toIntOrNull()
                    }
                }
            }

            binding.ivDropDown.safeClickListener {
                list[layoutPosition].let {
                    it.isSelected = !it.isSelected
                }
                notifyItemChanged(layoutPosition)
            }
            if (model.isSelected) {
                binding.ivDropDown.setImageDrawable(
                    ContextCompat.getDrawable(
                        rootContext,
                        R.drawable.ic_drop_down_medium_blue
                    )
                )
                binding.prescriptionDropDown.visibility = View.VISIBLE
                binding.tvMedicationName.setTextColor(
                    ContextCompat.getColor(
                        rootContext,
                        R.color.blue
                    )
                )
            } else {
                binding.ivDropDown.setImageDrawable(
                    ContextCompat.getDrawable(
                        rootContext,
                        R.drawable.ic_drop_down_grey
                    )
                )
                binding.prescriptionDropDown.visibility = View.GONE
                binding.tvMedicationName.setTextColor(
                    ContextCompat.getColor(
                        rootContext,
                        R.color.black
                    )
                )
            }

            binding.tvPrescribedDate.text = DateUtils.convertDateTimeToDate(
                model.createdAt,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                DateUtils.DATE_FORMAT_ddMMMyyyy
            )

            binding.tvDosageForm.text =
                model.dosageFormName.ifBlank { hypen }
            binding.tvBrand.text = model.brandName.ifBlank { hypen }
            binding.tvClassification.text = model.classificationName.ifBlank { hypen }
            binding.etInstruction.setText(model.instructionModified ?: model.instructionNote)
            binding.etInstruction.addTextChangedListener { editable ->
                list[layoutPosition].let {
                    if (editable.isNullOrBlank()) {
                        it.instructionModified = ""
                    } else {
                        it.instructionModified = editable.toString()
                    }
                    it.instructionUpdated =
                        !model.instructionNote.equals(it.instructionModified, false)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PrescriptionRefillViewHolder {
        return PrescriptionRefillViewHolder(
            LayoutPrescriptionRefillAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PrescriptionRefillViewHolder, position: Int) {
        list.let {
            holder.bind(position, it[position])
        }
    }

    fun getFormDosage(dosageFormName: String, context: Context): Drawable? {

        when (dosageFormName) {
            NCDMRUtil.Injection_Injectable_Solution -> {
                return ContextCompat.getDrawable(context, R.drawable.ic_injection_form)
            }

            NCDMRUtil.Liquid_Oral -> {
                return ContextCompat.getDrawable(context, R.drawable.ic_syrup)
            }

            NCDMRUtil.Tablet -> {
                return ContextCompat.getDrawable(context, R.drawable.ic_tablet)
            }

            NCDMRUtil.Capsule -> {
                return ContextCompat.getDrawable(context, R.drawable.ic_capsule)
            }
        }
        return null
    }
    fun getDosageValue(dosageUnitValue: String, dosageUnitName: String?) = dosageUnitValue + (dosageUnitName ?: "")

    override fun getItemCount(): Int {
        return list.size
    }

    fun setData(list: ArrayList<FillPrescriptionListResponse>) {
        this.list = list
    }
}