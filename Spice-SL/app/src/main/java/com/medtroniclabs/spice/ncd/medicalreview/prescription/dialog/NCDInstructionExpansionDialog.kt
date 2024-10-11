package com.medtroniclabs.spice.ncd.medicalreview.prescription.dialog

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.spice.appextensions.setWidth
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.FragmentNcdInstructionExpansionDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener


class NCDInstructionExpansionDialog : DialogFragment(), View.OnClickListener {


    private lateinit var binding: FragmentNcdInstructionExpansionDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNcdInstructionExpansionDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        initView()
    }

    private fun initView() {
        binding.btnDone.safeClickListener(this)
        binding.ivClose.safeClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.btnDone.id -> {
                dismiss()
            }

            binding.ivClose.id -> {
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
        setWidth(width)
    }


}