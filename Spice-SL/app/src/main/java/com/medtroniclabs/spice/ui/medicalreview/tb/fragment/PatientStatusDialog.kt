package com.medtroniclabs.spice.ui.medicalreview.tb.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setWidth
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.FragmentPatientStatusDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.DialogDismissListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PatientStatusDialog : DialogFragment(), View.OnClickListener {
    var listener: DialogDismissListener? = null
    private lateinit var binding: FragmentPatientStatusDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPatientStatusDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    companion object {
        const val TAG = "PatientStatusDialog"
        fun newInstance() =
            PatientStatusDialog()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        with(binding) {
            tvDiagnosisLbl.gone()
            tvSiteError.gone()
            tvSiteLbl.gone()
            siteChip.gone()
            btnCancel.safeClickListener(this@PatientStatusDialog)
            btnOkay.safeClickListener(this@PatientStatusDialog)
            ivClose.safeClickListener(this@PatientStatusDialog)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnOkay.id -> handleOkayClick()
            binding.btnCancel.id, binding.ivClose.id -> dismiss()
        }
    }

    private fun handleOkayClick() {

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