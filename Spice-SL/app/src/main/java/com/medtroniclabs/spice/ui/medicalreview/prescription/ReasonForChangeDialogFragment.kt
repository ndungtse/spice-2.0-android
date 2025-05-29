package com.medtroniclabs.spice.ui.medicalreview.prescription

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.setDialogWidthAndHeightAsWrapPercent
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams.Medications
import com.medtroniclabs.spice.common.DefinedParams.PrescribedMedicine
import com.medtroniclabs.spice.common.DefinedParams.Regimen
import com.medtroniclabs.spice.databinding.FragmentReasonForChangeDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseDialogFragment

class ReasonForChangeDialogFragment(
    private val callback: ReasonChangeCallback?
) : BaseDialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentReasonForChangeDialogBinding

    companion object {
        const val TAG = "ReasonForChangeDialogFragment"

        fun newInstance(
            name: String? = null,
            regimen: Int? = null,
            prescribedMedicine:Boolean = false,
            callback: ReasonChangeCallback?
        ): ReasonForChangeDialogFragment {
            val args = Bundle().apply {
                putString(Medications, name)
                regimen?.let {putInt(Regimen, it)  }
                putBoolean(PrescribedMedicine, prescribedMedicine)
            }
            return ReasonForChangeDialogFragment(callback).apply {
                arguments = args
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReasonForChangeDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListener()
        binding.btnCancel.setOnClickListener(this)
        binding.btnOkay.setOnClickListener(this)
    }

    private fun setListener() {
        binding.ivClose.safeClickListener(this)
        binding.btnCancel.safeClickListener(this)
        binding.etReason.doAfterTextChanged { input ->
            input?.let {
                isEnableRefer(if (it.trim().isNotEmpty()) it.trim().toString() else null)
            }
        }
    }

    private fun isEnableRefer(reason: String?) {
        binding.btnOkay.isEnabled = !reason.isNullOrEmpty()
    }

    private fun isAlreadyPrescribedMedicine(): Boolean {
        return arguments?.getBoolean(PrescribedMedicine, false) ?: false
    }

    private fun getRegimenLine(): String {
        return if (isAlreadyPrescribedMedicine()) {
            if (arguments?.getInt(Regimen) != null) {
                when(arguments?.getInt(Regimen)) {
                    1 -> "1st"
                    2 -> "2nd"
                    3 -> "3rd"
                    else -> "${arguments?.getString(Regimen)}th"
                }
            } else "1st"
        } else {
            "1 st"
        }
    }

    private fun initView() {
        binding.tvReferredReasonLabel.markMandatory()
        binding.tvName.text = arguments?.getString(Medications) ?: getString(R.string.seperator_hyphen)
        binding.tvRegimenLine.text = getRegimenLine()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnCancel, R.id.ivClose -> {
                dismiss()
            }
            R.id.btnOkay -> {
                val reason = binding.etReason.text.toString()
                callback?.onReasonProvided(reason)
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        handleDialogSize()
    }

    private fun handleDialogSize() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val width = if (CommonUtils.checkIsTablet(requireContext())) {
            if (isLandscape) 65 else 90
        } else {
            if (isLandscape) 65 else 90
        }
        setDialogWidthAndHeightAsWrapPercent(width)
    }

    interface ReasonChangeCallback {
        fun onReasonProvided(reason: String)
    }
}
