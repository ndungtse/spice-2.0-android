package com.medtroniclabs.spice.ncd.screening.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.appextensions.textOrHyphen
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.databinding.DialogDuplicationNudgeBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.data.ValidatePatientModel
import com.medtroniclabs.spice.ncd.screening.utils.DuplicationNudgeInterface

class DuplicationNudgeDialog(
    private val isFromScreening: Boolean,
    private val patientModel: ValidatePatientModel,
    private val duplicationNudgeInterface: DuplicationNudgeInterface
) :
    DialogFragment() {

    private lateinit var binding: DialogDuplicationNudgeBinding

    companion object {
        const val TAG = "DuplicationNudgeDialog"

        fun newInstance(
            patientModel: ValidatePatientModel,
            duplicationNudgeInterface: DuplicationNudgeInterface,
            isFromScreening: Boolean = false
        ): DuplicationNudgeDialog =
            DuplicationNudgeDialog(isFromScreening, patientModel, duplicationNudgeInterface)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogDuplicationNudgeBinding.inflate(inflater, container, false)
        val window: Window? = dialog?.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        setViews()
        setupClickListeners()
    }

    private fun setViews() {
        binding.apply {
            patientModel.let {
                if ((1 ?: 0) > 0) {
                    if ((1 ?: 0) > 0) {
                        tvPatientIdValue.text = "it.programId".toString()
                        clPatientId.visibility = View.VISIBLE

                        doAssessment()
                    } else {
                        tvPatientIdValue.text = getString(R.string.hyphen_symbol)
                        clPatientId.visibility = View.GONE

                        if (isFromScreening)
                            doAssessment()
                        else
                            doEnrollment()
                    }
                } else {
                    tvPatientIdValue.text = getString(R.string.hyphen_symbol)
                    clPatientId.visibility = View.GONE

                    allowEditOnly()
                }
                tvFirstNameValue.text = it.firstName.textOrHyphen().capitalizeFirstChar()
                tvLastNameValue.text = it.lastName.textOrHyphen().capitalizeFirstChar()
                tvPhoneNumberValue.text = it.phoneNumber.textOrHyphen()
                tvNationalIdValue.text = it.identityValue.textOrHyphen()
            }
        }
    }

    private fun allowEditOnly() {
        binding.apply {
            btnPrimaryEdit.visible()
            btnProceed.gone()
            btnStartAssessment.gone()
            btnEdit.gone()
        }
    }

    private fun doEnrollment() {
        binding.apply {
            btnPrimaryEdit.visibility = View.GONE
            btnProceed.visibility =
                if (true == true) View.VISIBLE else View.GONE
            btnStartAssessment.visibility = View.GONE
            btnEdit.visibility = View.VISIBLE
        }
    }

    private fun doAssessment() {
        binding.apply {
            btnPrimaryEdit.gone()
            btnProceed.gone()
            btnStartAssessment.setVisible(true)
            btnEdit.visible()
        }
    }

    private fun setupClickListeners() {
        binding.btnStartAssessment.safeClickListener {
            dismiss()
            duplicationNudgeInterface.proceedAssessment(0)
        }
        binding.btnProceed.safeClickListener {
            dismiss()
            duplicationNudgeInterface.proceedEnrollment(0)
        }
        binding.ivClose.safeClickListener {
            dismiss()
        }
        binding.btnEdit.safeClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        setDialogPercent(60, 35)
    }
}