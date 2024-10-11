package com.medtroniclabs.spice.ncd.medicalreview.prescription.dialog

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setWidth
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.FragmentNcdMedicationHistoryDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener


class NCDMedicationHistoryDialog : DialogFragment(),
    View.OnClickListener {

    private lateinit var binding: FragmentNcdMedicationHistoryDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNcdMedicationHistoryDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        initializeView()
        setListeners()
    }

    override fun onStart() {
        super.onStart()
        handleDialogSize()
    }

    private fun handleDialogSize() {
        setWidth(90)
    }

    private fun initializeView() {
        binding.rvMedicationHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.tvNoRecord.gone()
        binding.rvMedicationHistory.visible()
        binding.tvNoRecord.visible()
        binding.rvMedicationHistory.visible()
    }

    private fun setListeners() {
        binding.tvTitle.safeClickListener(this)
        binding.btnClose.safeClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            binding.tvTitle.id -> dismiss()
            binding.btnClose.id -> dismiss()
        }
    }
}