package com.medtroniclabs.spice.ui.medicalreview.hiv.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.google.android.material.textview.MaterialTextView
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.data.model.TreatmentItem
import com.medtroniclabs.spice.databinding.FragmentOpportunisticInfectionsTreatmentBinding
import com.medtroniclabs.spice.databinding.OpportunisticItemLayoutBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.OpportunisticInfectionViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.end
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.start
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OpportunisticInfectionsTreatmentFragment : BaseFragment() {

    private lateinit var binding: FragmentOpportunisticInfectionsTreatmentBinding
    private val viewModel: OpportunisticInfectionViewModel by activityViewModels()
    private var datePickerDialog: DatePickerDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            FragmentOpportunisticInfectionsTreatmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "OpportunisticInfectionsTreatmentFragment"
        fun newInstance(): OpportunisticInfectionsTreatmentFragment {
            return OpportunisticInfectionsTreatmentFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.llTreatmentList.visible()
        binding.tvNoHistory.gone()
        binding.llTreatmentList.removeAllViews()
        treatmentList.forEachIndexed { index, treatment ->
            val trimmedName = treatment.value.trim()
            val tagKey = "$trimmedName-Root"

            val itemBinding = OpportunisticItemLayoutBinding.inflate(layoutInflater).apply {
                root.tag = tagKey
                tvName.text = treatment.name.trim()

                val existing = viewModel.resultHashMap[trimmedName].orEmpty()
                tvStartDate.text = existing[start].takeUnless { it.isNullOrBlank() }
                    ?: getString(R.string.separator_double_hyphen)
                tvEndDate.text = existing[end].takeUnless { it.isNullOrBlank() }
                    ?: getString(R.string.separator_double_hyphen)

                tvStartDate.safeClickListener {
                    showDatePickerDialog(tvStartDate, trimmedName, isStart = true)
                }

                tvEndDate.safeClickListener {
                    showDatePickerDialog(tvEndDate, trimmedName, isStart = false)
                }

                if (index == treatmentList.size.minus(1)) {
                    divider.invisible()
                }
                // Enable End Date only if Start Date is set
                tvEndDate.isEnabled = !existing[start].isNullOrBlank()
                if (!tvEndDate.isEnabled) {
                    tvEndDate.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.grey_bg
                        )
                    )
                }
            }

            binding.llTreatmentList.addView(itemBinding.root)
        }
    }

    private fun showDatePickerDialog(tvDate: MaterialTextView, key: String, isStart: Boolean) {
        val initialDate: Triple<Int?, Int?, Int?>? =
            tvDate.text?.takeIf { it.isNotBlank() }?.let {
                DateUtils.convertedMMMToddMM(it.toString())
            }

        val minDate: Long? = if (!isStart) {
            viewModel.resultHashMap[key]?.get(start)?.takeIf { it.isNotBlank() }?.let {
                DateUtils.convertToMillis(it, DateUtils.DATE_ddMMyyyy)
            }
        } else null

        if (datePickerDialog != null) return

        datePickerDialog = ViewUtils.showDatePicker(
            context = requireContext(),
            minDate = minDate,
            date = initialDate,
            cancelCallBack = { datePickerDialog = null }
        ) { _, year, month, day ->
            val formattedDate = DateUtils.convertDateTimeToDate(
                "$day-$month-$year",
                DateUtils.DATE_FORMAT_ddMMyyyy,
                DateUtils.DATE_ddMMyyyy
            )

            tvDate.text = formattedDate
            val result =
                viewModel.resultHashMap.getOrPut(key) { hashMapOf(start to "", end to "") }

            if (isStart) {
                result[start] = formattedDate
                result[end] = ""

                val itemView = binding.llTreatmentList.findViewWithTag<View>("$key-Root")
                val endDateView = itemView?.findViewById<MaterialTextView>(R.id.tvEndDate)
                updateEndDateViewState(endDateView, isEnabled = true)
            } else {
                result[end] = formattedDate
            }
            setFragmentResult(
                MedicalReviewDefinedParams.HIV_STATUS, bundleOf(
                    MedicalReviewDefinedParams.CHIP_ITEMS to true
                )
            )
            datePickerDialog = null
        }
    }

    private fun updateEndDateViewState(view: MaterialTextView?, isEnabled: Boolean) {
        view?.apply {
            this.isEnabled = isEnabled
            text = context.getString(R.string.separator_double_hyphen)
            setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isEnabled) R.color.black else R.color.grey_bg
                )
            )
            setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
    }

    private val treatmentList = listOf(
        TreatmentItem("TB Preventive Treatment (TPT)", value = "tbPreventiveDateRange"),
        TreatmentItem("Cotrimoxazole (CTX)", value = "cotrimoxazoleDateRange"),
        TreatmentItem("TB Treatment", value = "tbTreatmentDateRange"),
        TreatmentItem("Cryptococcal Meningitis", value = "cryptococcalMeningitisDateRange")
    )

    fun validateInput(): Boolean {
        return hasAnyStartOrEndDateFilled(viewModel.resultHashMap)
    }

    private fun hasAnyStartOrEndDateFilled(resultMap: HashMap<String, HashMap<String, String>>): Boolean {
        for ((_, dateMap) in resultMap) {
            val startNotBlank = dateMap[start]?.isNotBlank() == true
            val endNotBlank = dateMap[end]?.isNotBlank() == true
            if (startNotBlank || endNotBlank) return true
        }
        return false
    }
}
