package com.medtroniclabs.spice.ui.mypatients.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.ViewUtils
import com.medtroniclabs.spice.databinding.FragmentPregnancyDetailsBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.utility.CustomSpinnerAdapter
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.activity.MotherNeonateANCActivity
import com.medtroniclabs.spice.ui.mypatients.viewmodel.MotherNeonateANCViewModel

class PregnancyDetailsFragment : BaseFragment(), View.OnClickListener {

    private lateinit var binding: FragmentPregnancyDetailsBinding
    private var datePickerDialog: DatePickerDialog? = null
    private val viewModel: MotherNeonateANCViewModel by activityViewModels()

    companion object {
        const val TAG = "PregnancyDetailsFragment"
        fun newInstance(): PregnancyDetailsFragment {
            return PregnancyDetailsFragment()
        }
    }

    fun isAnyEditTextFilled(): Boolean {
        return binding.etHeight.text?.isNotBlank() == true ||
                binding.etWeight.text?.isNotBlank() == true ||
                binding.etSystolic.text?.isNotBlank() == true ||
                binding.etDiastolic.text?.isNotBlank() == true ||
                binding.etPulse.text?.isNotBlank() == true ||
                binding.tvLastMenstrualPeriodDate.text?.isNotBlank() == true ||
                binding.tvEstimatedDeliveryDate.text?.isNotBlank() == true ||
                binding.etGestationalAge.text?.isNotBlank() == true ||
                binding.etNoOfFetus.text?.isNotBlank() == true ||
                binding.etGravida.text?.isNotBlank() == true ||
                binding.etParityFirst.text?.isNotBlank() == true ||
                binding.etParitySecond.text?.isNotBlank() == true ||
                viewModel.selectedBloodGroup != null
    }

    private fun isHeightValid(): Boolean {
        return isValidInput(
            binding.etHeight.text.toString(),
            binding.etHeight,
            binding.tvHeightError,
            50.0..300.0,
            R.string.height_error
        )
    }

    private fun isWeightValid(): Boolean {
        return isValidInput(
            binding.etWeight.text.toString(),
            binding.etWeight,
            binding.tvKgError,
            10.0..400.0,
            R.string.weight_error
        )
    }

    private fun isValidInput(
        inputText: String,
        editText: EditText,
        errorTextView: TextView,
        validRange: ClosedRange<Double>,
        errorMessageResId: Int
    ): Boolean {
        val input = inputText.toDoubleOrNull()
        if (editText.text.isNullOrBlank()) {
            errorTextView.gone()
            return true
        }
        if (!(input != null && input in validRange)) {
            errorTextView.visible()
            errorTextView.text = editText.context.getString(errorMessageResId)
            return false
        }
        errorTextView.gone()
        return true
    }

    fun validateInput(): Boolean {
        val isWeightValid = isWeightValid()
        val isHeightValid = isHeightValid()
        val isSystolicValid = isValidMeasurement(
            binding.etSystolic.text.toString(),
            binding.tvSystolicError,
            30,
            300,
            binding.etDiastolic,
            minErrorMessage = getString(R.string.systolic_error_min),
            maxErrorMessage = getString(R.string.systolic_error_max)
        )
        val isDiastolicValid = isValidMeasurement(
            binding.etDiastolic.text.toString(),
            binding.tvDiastolicError,
            30,
            300,
            binding.etDiastolic,
            getString(R.string.diastolic_error_min),
            maxErrorMessage = getString(R.string.diastolic_error_max)
        )
        val isPulseValid = isValidMeasurement(
            binding.etPulse.text.toString(),
            binding.tvPulseError,
            35,
            300,
            minErrorMessage = getString(R.string.pulse_error_min),
            maxErrorMessage = getString(R.string.pulse_error_max)
        )
        val isGestationalAgeValid = isBasicValid(
            binding.etGestationalAge.text.toString(),
            binding.tvGestationalAgeError,
            0,
            getString(R.string.error_label),
            60,
            getString(R.string.gestational_error)
        )
        val isNoFoFetusValid = isBasicValid(
            binding.etNoOfFetus.text.toString(),
            binding.tvNoOfFetusError,
            0,
            getString(R.string.error_label)
        )
        val isGravidaValid = isBasicValid(
            binding.etGravida.text.toString(),
            binding.tvGravidaError,
            0,
            getString(R.string.error_label)
        )
        val isParityFirstValid = isBasicValid(
            binding.etParityFirst.text.toString(),
            binding.tvParityFirstError,
            0,
            getString(R.string.error_label)
        )
        val isParitySecondValid = isBasicValid(
            binding.etParitySecond.text.toString(),
            binding.tvParitySecondError,
            0,
            getString(R.string.error_label)
        )
        // Return true only if all validations pass
        return isWeightValid && isHeightValid && isSystolicValid && isDiastolicValid && isPulseValid && isGestationalAgeValid && isNoFoFetusValid && isGravidaValid && isParityFirstValid && isParitySecondValid
    }

    private fun isBasicValid(
        valueText: String?,
        errorTextView: TextView,
        minValue: Int,
        errorMessage: String,
        maxValue: Int? = null,
        maxValueError: String? = null,
    ): Boolean {
        val value = valueText?.toIntOrNull()
        if (value == null) {
            // Invalid input, display error message
            errorTextView.gone()
            return true
        }
        if (value == minValue) {
            // Value is less than minimum allowed value, display error message
            errorTextView.text = errorMessage
            errorTextView.visible()
            return false
        }

        if (maxValue != null && value > maxValue) {
            errorTextView.text = maxValueError ?: getString(R.string.error)
            errorTextView.visible()
            return false
        }
        // Valid input
        errorTextView.gone()
        return true
    }

    private fun isValidMeasurement(
        valueText: String?,
        errorTextView: TextView,
        minValue: Int,
        maxValue: Int,
        text: AppCompatEditText? = null,
        minErrorMessage: String,
        maxErrorMessage: String,
    ): Boolean {
        val value = valueText?.toIntOrNull()
        val diastolic = text?.text.toString().toIntOrNull()
        if (value == null) {
            // Invalid input, display error message
            errorTextView.gone()
            return true
        }
        if (value < minValue) {
            // Value is less than minimum allowed value, display error message
            errorTextView.text = minErrorMessage
            errorTextView.visible()
            return false
        }
        if (value > maxValue) {
            errorTextView.text = maxErrorMessage
            errorTextView.visible()
            return false
        }

        if (diastolic != null && value < diastolic) {
            errorTextView.text = getText(R.string.systolic_diastolic_error)
            errorTextView.visible()
            return false
        }
        // Valid input
        errorTextView.gone()
        return true
    }

    fun getPregnancyDetailsFromEditText() {
        if (validateInput()) {
            with(binding) {
                viewModel.pregnancyDetailsModel.apply {
                    height = etHeight.text.toString().toDoubleOrNull()
                    weight = etWeight.text.toString().toDoubleOrNull()
                    bloodPressure = etSystolic.text.toString().toDoubleOrNull()
                    pulse = etPulse.text.toString().toIntOrNull()
                    lastMenstrualPeriod = tvLastMenstrualPeriodDate.text.toString()
                    estimatedDeliveryDate = tvEstimatedDeliveryDate.text.toString()
                    gestationalAge = etGestationalAge.text.toString()
                    noOfFetus = etNoOfFetus.text.toString().toIntOrNull()
                    patientBloodGroup = viewModel.selectedBloodGroup
                    gravida = etGravida.text.toString().toIntOrNull()
                    parity = etParityFirst.text.toString().toIntOrNull()
                    systolic = etSystolic.text.toString().toDoubleOrNull()
                    diastolic = etDiastolic.text.toString().toDoubleOrNull()
                    bmi = calculateBMI()
                }
            }
        }
    }

    private fun calculateBMI(): String? {
        val height = binding.etHeight.text.toString().toDoubleOrNull()
        val weight = binding.etWeight.text.toString().toDoubleOrNull()
        return if (height != null && weight != null) {
            CommonUtils.getBMI(
                weight,
                height,
                requireContext()
            )
        } else {
            null
        }
    }


    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Not needed for your use case
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Not needed for your use case
        }

        override fun afterTextChanged(s: Editable?) {
            // Call the method to check if any EditText field is filled
            if (s == binding.etHeight.text || s == binding.etWeight.text) {
                updateBMI()
            }
            (requireActivity() as? MotherNeonateANCActivity)?.updateNextButtonState()
        }
    }

    private fun updateGestationalAge() {
        val weeks = binding.etGestationalAge.text.toString().toIntOrNull()
        if (weeks != null && weeks > 0) {
            binding.etGestationalAge.setText(
                getString(
                    R.string.no_of_weeks,
                    weeks.toString()
                )
            )
        }
    }


    private fun updateBMI() {
        val height = binding.etHeight.text.toString().toDoubleOrNull()
        val weight = binding.etWeight.text.toString().toDoubleOrNull()
        val bmiValue = if (height != null && weight != null) {
            CommonUtils.getBMI(height, weight, requireContext())
        } else {
            getString(R.string.hyphen_symbol)
        }
        binding.tvBMIText.text = bmiValue
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPregnancyDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
    }

    private fun attachObservers() {
        viewModel.ancMetaLiveDataForBloodGroup.observe(viewLifecycleOwner) {
            val complaintList = ArrayList<Map<String, Any>>()
            for (i in it) {
                complaintList.add(
                    CommonUtils.getOptionMap(i.name, i.name)
                )
            }
            setSpinner(complaintList)
        }
    }

    private fun initView() {
        viewModel.setAncReqToGetMetaForBloodGroup(MedicalReviewTypeEnums.BloodGroup.name)
        with(binding) {
            etHeight.addTextChangedListener(textWatcher)
            etWeight.addTextChangedListener(textWatcher)
            etSystolic.addTextChangedListener(textWatcher)
            etDiastolic.addTextChangedListener(textWatcher)
            etPulse.addTextChangedListener(textWatcher)
            tvLastMenstrualPeriodDate.addTextChangedListener(textWatcher)
            tvEstimatedDeliveryDate.addTextChangedListener(textWatcher)
            etGestationalAge.addTextChangedListener(textWatcher)
            etNoOfFetus.addTextChangedListener(textWatcher)
            etGravida.addTextChangedListener(textWatcher)
            etParityFirst.addTextChangedListener(textWatcher)
            etParitySecond.addTextChangedListener(textWatcher)
            tvLastMenstrualPeriodDate.safeClickListener(this@PregnancyDetailsFragment)
            tvEstimatedDeliveryDate.safeClickListener(this@PregnancyDetailsFragment)
        }
    }

    private fun setSpinner(complaintList: ArrayList<Map<String, Any>>) {
        val dropDownList = ArrayList<Map<String, Any>>()
        dropDownList.add(
            hashMapOf<String, Any>(
                DefinedParams.NAME to DefinedParams.DefaultIDLabel,
                DefinedParams.id to DefinedParams.DefaultID
            )
        )
        dropDownList.addAll(complaintList)
        val adapter = CustomSpinnerAdapter(requireContext())
        adapter.setData(dropDownList)
        binding.etPatientBloodGroup.adapter = adapter
        binding.etPatientBloodGroup.setSelection(0, false)
        binding.etPatientBloodGroup.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    itemId: Long
                ) {
                    val selectedItem = adapter.getData(position = pos)
                    selectedItem?.let {
                        val selectedId = it[DefinedParams.id] as String?
                        val selectedBloodGroup = it[DefinedParams.NAME] as String?
                        if (selectedId != DefinedParams.DefaultID) {
                            viewModel.selectedBloodGroup = selectedBloodGroup
                            (requireActivity() as? MotherNeonateANCActivity)?.updateNextButtonState()
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /**
                     * this method is not used
                     */
                }
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.tvLastMenstrualPeriodDate.id -> {
                showDatePickerDialog(binding.tvLastMenstrualPeriodDate, true)
            }

            binding.tvEstimatedDeliveryDate.id -> {
                showDatePickerDialog(binding.tvEstimatedDeliveryDate)
            }
        }
    }

    private fun showDatePickerDialog(textView: AppCompatTextView, disableFuture: Boolean = false) {
        var yearMonthDate: Triple<Int?, Int?, Int?>? = null
        if (!textView.text.isNullOrBlank())
            yearMonthDate =
                DateUtils.convertedMMMToddMM(textView.text.toString())
        if (datePickerDialog == null) {
            datePickerDialog = ViewUtils.showDatePicker(
                context = requireContext(),
                minDate = null,
                date = yearMonthDate,
                disableFutureDate = disableFuture,
                cancelCallBack = { datePickerDialog = null }
            ) { _, year, month, dayOfMonth ->
                val stringDate = "$dayOfMonth-$month-$year"
                textView.text =
                    DateUtils.convertDateTimeToDate(
                        stringDate,
                        DateUtils.DATE_FORMAT_ddMMyyyy,
                        DateUtils.DATE_ddMMyyyy
                    )
                datePickerDialog = null
            }
        }
    }
}