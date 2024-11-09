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
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.StringConverter
import com.medtroniclabs.spice.databinding.DialogDuplicationNudgeBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.mappingkey.Screening

class DuplicationNudgeDialog(private val callback: () -> Unit) :
    DialogFragment(), View.OnClickListener {

    private lateinit var binding: DialogDuplicationNudgeBinding

    companion object {
        const val TAG = "DuplicationNudgeDialog"
        const val PATIENT_INFO = "PatientInfo"

        fun newInstance(
            patientInfo: String?,
            callback: () -> Unit
        ): DuplicationNudgeDialog {
            val args = Bundle()
            args.putString(PATIENT_INFO, patientInfo)
            val fragment = DuplicationNudgeDialog(callback)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
    }

    private fun setViews() {
        requireArguments().getString(PATIENT_INFO)?.let { patientInfo ->
            StringConverter.convertStringToMap(patientInfo)?.let { map ->
                binding.apply {
                    tvFirstNameValue.text = map[Screening.firstName]?.toString().textOrHyphen()
                    tvLastNameValue.text = map[Screening.lastName]?.toString().textOrHyphen()
                    tvPhoneNumberValue.text = map[Screening.phoneNumber]?.toString().textOrHyphen()
                    tvNationalIdValue.text = map[Screening.identityValue]?.toString().textOrHyphen()

                    val patientId = map[DefinedParams.PatientReference]?.toString()
                    tvPatientIdValue.text = patientId.textOrHyphen()
                    clPatientId.setVisible(!patientId.isNullOrBlank())

                    doAssessment()

                    btnEdit.safeClickListener(this@DuplicationNudgeDialog)
                    btnPrimary.safeClickListener(this@DuplicationNudgeDialog)
                    ivClose.safeClickListener(this@DuplicationNudgeDialog)
                }
            }
        }
    }

    private fun allowEditOnly() {
        binding.apply {
            btnEdit.visible()
            btnPrimary.gone()
        }
    }

    private fun doEnrollment() {
        binding.apply {
            primaryGroup.visible()
            btnPrimary.text = getString(R.string.proceed)
        }
    }

    private fun doAssessment() {
        binding.apply {
            primaryGroup.visible()
            btnPrimary.text = getString(R.string.start_assessment)
        }
    }

    override fun onStart() {
        super.onStart()
        setDialogPercent(60, 35)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnPrimary -> {
                dismiss()
                callback.invoke()
            }

            R.id.btnEdit, R.id.ivClose -> {
                dismiss()
            }
        }
    }
}