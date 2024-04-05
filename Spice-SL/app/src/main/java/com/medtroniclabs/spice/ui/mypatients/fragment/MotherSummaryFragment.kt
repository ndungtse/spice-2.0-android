package com.medtroniclabs.spice.ui.mypatients.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentMotherSummaryBinding
import com.medtroniclabs.spice.ui.BaseFragment

class MotherSummaryFragment : BaseFragment() {

    private lateinit var binding : FragmentMotherSummaryBinding
    private var datePickerDialog : DatePickerDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMotherSummaryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeDatePicker()
    }

    private fun initializeDatePicker() {
        binding.tvNextVisitDate.setOnClickListener {
            initializeNextVisitDate()
        }
    }

    private fun initializeNextVisitDate() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (binding.tvNextVisitDate.text.toString().isNotEmpty())
            yearMonthDate = DateUtils.convertddMMMToddMM(binding.tvNextVisitDate.text.toString())
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                requireContext(),
                minDate = DateUtils.getTomorrowDate(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.tvNextVisitDate.text =
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DateUtils.DATE_FORMAT_ddMMyyyy,
                        DateUtils.DATE_ddMMyyyy
                    )
                datePickerDialog = null
            }
        }
    }

    companion object {
        const val TAG = "MotherSummaryFragment"

        fun newInstance():MotherSummaryFragment {
            return MotherSummaryFragment()
        }
    }
}