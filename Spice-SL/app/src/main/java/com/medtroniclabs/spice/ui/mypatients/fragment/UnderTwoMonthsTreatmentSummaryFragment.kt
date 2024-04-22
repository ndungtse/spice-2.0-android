package com.medtroniclabs.spice.ui.mypatients.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentUnderTwoMonthsTreatmentSummaryBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.undertwomonths.UnderTwoMonthViewModel

class UnderTwoMonthsTreatmentSummaryFragment : BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentUnderTwoMonthsTreatmentSummaryBinding
    private var datePickerDialog: DatePickerDialog? = null
    private val viewModel: UnderTwoMonthViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUnderTwoMonthsTreatmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinnerForDeliveryType()
        setListeners()
    }

    private fun setListeners() {
        binding.etNextVisitTime.safeClickListener(this)
    }

    private fun spinnerForDeliveryType() {
        val list = arrayListOf<Map<String, Any>>()
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to "Select",
                DefinedParams.ID to DefinedParams.DefaultSelectID
            )
        )
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to "Status 1",
                DefinedParams.ID to "1L"
            )
        )
        list.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to "Status 2",
                DefinedParams.ID to "2L"
            )
        )
        setListenerToDeliveryStatus(list)
    }

    private fun setListenerToDeliveryStatus(list: ArrayList<Map<String, Any>>) {
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(list)
        binding.tvPatientStatus.adapter = adapter
    }

    private fun showDatePickerDialog() {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!binding.etNextVisitTime.text.isNullOrBlank())
            yearMonthDate =
                DateUtils.convertedMMMToddMM(binding.etNextVisitTime.text.toString())
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                minDate = DateUtils.getTomorrowDate(),
                date = yearMonthDate,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                binding.etNextVisitTime.text =
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DateUtils.DATE_FORMAT_ddMMyyyy,
                        DateUtils.DATE_ddMMyyyy
                    )
                viewModel.nextVisitDateHashMap[DefinedParams.NextVisitDate] =
                    binding.etNextVisitTime.text.toString()
                datePickerDialog = null
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.etNextVisitTime.id -> {
                showDatePickerDialog()
            }
        }
    }
}