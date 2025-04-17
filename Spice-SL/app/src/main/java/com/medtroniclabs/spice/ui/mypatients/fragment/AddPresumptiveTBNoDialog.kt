package com.medtroniclabs.spice.ui.mypatients.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setWidth
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.FragmentAddWeightDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener

class AddPresumptiveTBNoDialog : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentAddWeightDialogBinding

    var listener: OnPresumptiveTBEnteredListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddWeightDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }


    companion object {
        const val TAG = "AddPresumptiveTBNoDialog"
        const val Data = "Data"
        fun newInstance(text: String? = null): AddPresumptiveTBNoDialog {
            val fragment = AddPresumptiveTBNoDialog()
            fragment.arguments = Bundle().apply {
                putString(Data, text)
            }
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        with(binding) {
            tvTitle.text = getString(R.string.presumptive_tb_no)
            tvWeightLabel.text = getString(R.string.presumptive_tb_no)
            etWeight.hint = getString(R.string.enter)
            binding.etWeight.apply {
                inputType = InputType.TYPE_CLASS_TEXT
                filters = arrayOf(InputFilter.LengthFilter(15))
            }
            btnCancel.safeClickListener(this@AddPresumptiveTBNoDialog)
            etWeight.addTextChangedListener(textWatcher)
            btnOkay.safeClickListener(this@AddPresumptiveTBNoDialog)
            ivClose.safeClickListener(this@AddPresumptiveTBNoDialog)
            arguments?.getString(Data)?.let { text ->
                if (text.isNotBlank()) {
                    etWeight.setText(text)
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnOkay.id -> handleOkayClick()
            binding.btnCancel.id, binding.ivClose.id -> dismiss()
        }
    }

    private fun handleOkayClick() {
        val tbNumber = binding.etWeight.text.toString().trim()
        if (tbNumber.isNotEmpty()) {
            listener?.onPresumptiveTBEntered(tbNumber)
            dismiss()
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
            val hasString = (s?.trim()?.count() ?: 0) > 0
            binding.btnOkay.isEnabled = hasString
        }
    }

    private fun handleDialogSize() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val width = if (CommonUtils.checkIsTablet(requireContext())) {
            if (isLandscape) 65 else 90
        } else {
            if (isLandscape) 65 else 90
        }
        setWidth(width)
    }

    override fun onStart() {
        super.onStart()
        handleDialogSize()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleDialogSize()
    }
}

interface OnPresumptiveTBEnteredListener {
    fun onPresumptiveTBEntered(tbNumber: String)
}