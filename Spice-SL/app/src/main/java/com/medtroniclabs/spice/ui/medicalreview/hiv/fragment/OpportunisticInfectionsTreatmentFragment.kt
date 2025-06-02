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
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.OpportunisticInfectionViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.cotrimoxazoleDateRange
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.cryptococcalMeningitisDateRange
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.endDate
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.startDate
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.tbPreventiveDateRange
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.tbTreatmentDateRange
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OpportunisticInfectionsTreatmentFragment : BaseFragment() {

    private lateinit var binding: FragmentOpportunisticInfectionsTreatmentBinding
    private val viewModel: OpportunisticInfectionViewModel by activityViewModels()
    private var datePickerDialog: DatePickerDialog? = null
    private val patientViewModel: PatientDetailViewModel by activityViewModels()

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
        viewModel.getOpportunisticInfection(memberId = patientViewModel.getPatientMemberId(), patientReference = patientViewModel.getPatientFHIRId())
        initView()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.getOpportunisticInfection.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showProgress()
                }

                ResourceState.SUCCESS -> {
                    hideProgress()
                    resourceState.data?.let { result ->
                        result.forEach { (key, valueMap) ->
                            if (valueMap != null && valueMap.containsKey(startDate)) {
                                val startDate = valueMap[startDate]
                                if (!startDate.isNullOrBlank()) {
                                    val formattedMap = hashMapOf<String, String>()
                                    formattedMap[MedicalReviewDefinedParams.startDate] =
                                        DateUtils.convertDateTimeToDate(
                                            startDate,
                                            DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                                            DateUtils.DATE_ddMMyyyy
                                        )

                                    if (valueMap.containsKey(endDate)) {
                                        val endDate = valueMap[endDate]
                                        if (!endDate.isNullOrBlank()){
                                            formattedMap[MedicalReviewDefinedParams.endDate] =
                                                DateUtils.convertDateTimeToDate(
                                                    endDate,
                                                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                                                    DateUtils.DATE_ddMMyyyy
                                                )
                                        }
                                    }
                                    viewModel.resultHashMap[key] = formattedMap
                                }
                            }
                        }
                        initView()
                    }
                }
                ResourceState.ERROR -> {
                    hideProgress()
                }
            }
        }
    }

    private fun initView() {
        binding.llTreatmentList.visible()
        binding.tvNoHistory.gone()
        binding.llTreatmentList.removeAllViews()
        val treatmentList = listOf(
            TreatmentItem(
                requireContext().getString(R.string.tb_preventive_treatment),
                value = tbPreventiveDateRange
            ),
            TreatmentItem(
                requireContext().getString(R.string.cotrimoxazole),
                value = cotrimoxazoleDateRange
            ),
            TreatmentItem(
                requireContext().getString(R.string.tb_treatment),
                value = tbTreatmentDateRange
            ),
            TreatmentItem(
                requireContext().getString(R.string.cryptococcal_meningitis),
                value = cryptococcalMeningitisDateRange
            )
        )
        treatmentList.forEachIndexed { index, treatment ->
            val trimmedName = treatment.value.trim()
            val tagKey = "$trimmedName-Root"

            val itemBinding = OpportunisticItemLayoutBinding.inflate(layoutInflater).apply {
                root.tag = tagKey
                tvName.text = treatment.name.trim()

                val existing = viewModel.resultHashMap[trimmedName].orEmpty()
                tvStartDate.text = existing[startDate].takeUnless { it.isNullOrBlank() }
                    ?: getString(R.string.separator_double_hyphen)
                tvEndDate.text = existing[endDate].takeUnless { it.isNullOrBlank() }
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
                tvEndDate.isEnabled = !existing[startDate].isNullOrBlank()
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
            viewModel.resultHashMap[key]?.get(startDate)?.takeIf { it.isNotBlank() }?.let {
                DateUtils.convertToMillis(it, DateUtils.DATE_ddMMyyyy)
            }
        } else null

        if (datePickerDialog != null) return

        datePickerDialog = ViewUtils.showDatePicker(
            context = requireContext(),
            minDate = minDate,
            date = initialDate,
            disableFutureDate = true,
            cancelCallBack = { datePickerDialog = null }
        ) { _, year, month, day ->
            val formattedDate = DateUtils.convertDateTimeToDate(
                "$day-$month-$year",
                DateUtils.DATE_FORMAT_ddMMyyyy,
                DateUtils.DATE_ddMMyyyy
            )

            tvDate.text = formattedDate
            val result =
                viewModel.resultHashMap.getOrPut(key) { hashMapOf(startDate to "", endDate to "") }

            if (isStart) {
                result[startDate] = formattedDate
                result[endDate] = ""

                val itemView = binding.llTreatmentList.findViewWithTag<View>("$key-Root")
                val endDateView = itemView?.findViewById<MaterialTextView>(R.id.tvEndDate)
                updateEndDateViewState(endDateView, isEnabled = true)
            } else {
                result[endDate] = formattedDate
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

    fun validateInput(): Boolean {
        return hasAnyStartOrEndDateFilled(viewModel.resultHashMap)
    }

    private fun hasAnyStartOrEndDateFilled(resultMap: HashMap<String, HashMap<String, String>>): Boolean {
        for ((_, dateMap) in resultMap) {
            val startNotBlank = dateMap[startDate]?.isNotBlank() == true
            val endNotBlank = dateMap[endDate]?.isNotBlank() == true
            if (startNotBlank || endNotBlank) return true
        }
        return false
    }
}
