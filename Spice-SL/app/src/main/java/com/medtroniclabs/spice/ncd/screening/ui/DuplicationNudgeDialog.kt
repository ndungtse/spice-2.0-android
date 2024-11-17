package com.medtroniclabs.spice.ncd.screening.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.setDialogPercent
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.appextensions.textOrEmpty
import com.medtroniclabs.spice.appextensions.textOrHyphen
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
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
        const val IS_FROM_ENROLLMENT = "isFromEnrollment"

        fun newInstance(
            patientInfo: String?,
            isFromEnrollment: Boolean,
            callback: () -> Unit
        ): DuplicationNudgeDialog {
            val args = Bundle()
            args.putString(PATIENT_INFO, patientInfo)
            args.putBoolean(IS_FROM_ENROLLMENT, isFromEnrollment)
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
        val isFromEnrollment = requireArguments().getBoolean(IS_FROM_ENROLLMENT)

        requireArguments().getString(PATIENT_INFO)?.let { patientInfo ->
            StringConverter.convertStringToMap(patientInfo)?.let { map ->
                binding.apply {
                    val patientId = map[DefinedParams.PatientReference]?.toString()
                    val patientStatus = map[AnalyticsDefinedParams.PatientStatus]?.toString()

                    val sameOperatingUnit = map[Screening.CHIEFDOM_ID]?.toString().equals(SecuredPreference.getChiefdomId().toString())
                    val sameAccount = map[Screening.DISTRICT_ID]?.toString().equals(SecuredPreference.getDistrictId().toString())

                    tvFirstNameValue.text = map[Screening.firstName]?.toString().textOrHyphen()
                    tvLastNameValue.text = map[Screening.lastName]?.toString().textOrHyphen()
                    tvPhoneNumberValue.text = map[Screening.phoneNumber]?.toString().textOrHyphen()
                    tvNationalIdValue.text = map[Screening.identityValue]?.toString().textOrHyphen()
                    clPatientId.setVisible(!patientId.isNullOrBlank())
                    tvPatientIdValue.text = patientId.textOrHyphen()

                    if (isFromEnrollment) {
                        if (patientStatus.equals(DefinedParams.SCREENED, true))
                            doEnrollment(sameAccount)
                        else
                            doAssessment(sameOperatingUnit)
                    } else
                        doAssessment(sameOperatingUnit)

                    btnEdit.safeClickListener(this@DuplicationNudgeDialog)
                    btnPrimary.safeClickListener(this@DuplicationNudgeDialog)
                    ivClose.safeClickListener(this@DuplicationNudgeDialog)
                }
            }
        }
    }

    private fun doEnrollment(sameAccount: Boolean) {
        binding.apply {
            primaryGroup.setVisible(sameAccount)
            btnPrimary.text = getString(R.string.proceed)
        }
    }

    private fun doAssessment(sameOperatingUnit: Boolean) {
        binding.apply {
            primaryGroup.setVisible(sameOperatingUnit)
            btnPrimary.text = getString(R.string.start_assessment)
        }
    }

    override fun onStart() {
        super.onStart()
        if (CommonUtils.checkIsTablet(requireContext()))
            setDialogPercent(80, 40)
        else
            setDialogPercent(95, 50)
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