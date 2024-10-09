package com.medtroniclabs.spice.ncd.medicalreview.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentNcdMedicalReviewDiagnosisCardBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.medicalreview.NCDDialogDismissListener
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.IS_FEMALE
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.IS_INITIAL_MR
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.MENU_ID
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDDiagnosisDialogFragment
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDPatientHistoryDialog
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.ArrayList

@AndroidEntryPoint
class NCDMedicalReviewDiagnosisCardFragment : BaseFragment(), View.OnClickListener,
    NCDDialogDismissListener {
    private val patientDetailViewModel: PatientDetailViewModel by activityViewModels()
    private lateinit var binding: FragmentNcdMedicalReviewDiagnosisCardBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNcdMedicalReviewDiagnosisCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "NCDMedicalReviewDiagnosisCardFragment"
        fun newInstance(isInitial: Boolean, isFemale: Boolean, menu: String?) =
            NCDMedicalReviewDiagnosisCardFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(IS_INITIAL_MR, isInitial)
                    putBoolean(IS_FEMALE, isFemale)
                    putString(MENU_ID, menu)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun getInitialMr(): Boolean {
        return arguments?.getBoolean(IS_INITIAL_MR) ?: false
    }

    private fun getIsFemale(): Boolean {
        return arguments?.getBoolean(IS_FEMALE) ?: false
    }

    private fun getTypeForDiagnoses(): ArrayList<String> {
        val type = getMenu()
        val baseList = arrayListOf(
            NCDMRUtil.SUBSTANCE_DISORDER,
            NCDMRUtil.MENTALHEALTH,
            NCDMRUtil.HYPERTENSION,
            NCDMRUtil.DIABETES,
            NCDMRUtil.HIV
        )
        return when (type) {
            NCDMRUtil.NCD.lowercase(), NCDMRUtil.MENTAL_HEALTH.lowercase() -> baseList
            DefinedParams.PregnancyANC.lowercase() -> baseList.apply { add(NCDMRUtil.PREGNANCY) }
            else -> arrayListOf()
        }
    }

    private fun getMenu(): String? {
        return arguments?.getString(MENU_ID)?.lowercase()
    }

    private fun initView() {
        val hyphen = getString(R.string.hyphen_symbol)
        binding.apply {
            val isContinuous = getInitialMr()
            val isMaternal = !getInitialMr() && getIsFemale() && (getMenu().equals(DefinedParams.PregnancyANC,true))
            val isNCDAndMentalHealth = !getInitialMr()

            // Hide all cards initially
            pregnancyCard.root.setVisible(false)
            weightCard.root.setVisible(false)
            eddCard.root.setVisible(false)
            patientStatusCard.root.setVisible(false)
            when {
                isContinuous -> {
                    // All cards remain hidden
                }

                isMaternal -> {
                    pregnancyCard.root.setVisible(true)
                    weightCard.root.setVisible(true)
                    eddCard.root.setVisible(true)
                }

                isNCDAndMentalHealth -> {
                    if (getMenu().equals(NCDMRUtil.NCD, true)) {
                        patientStatusCard.root.setVisible(true)
                    }
                }
            }

            diagnosisCard.tvDiagnosisLbl.text = getString(R.string.diagnosis)
            diagnosisCard.tvDiagnosis.text = hyphen
            diagnosisCard.tvDiagnosisConfirm.text = getString(R.string.confirm_diagnoses)

            bpCard.tvDiagnosisLbl.text = getString(R.string.blood_pressure)
            bpCard.tvDiagnosis.text = hyphen
            bpCard.tvDiagnosisConfirm.text = getString(R.string.view_details)

            bgCard.tvDiagnosisLbl.text = getString(R.string.blood_glucose)
            bgCard.tvDiagnosis.text = hyphen
            bgCard.tvDiagnosisConfirm.text = getString(R.string.view_details)

            pregnancyCard.tvDiagnosisLbl.text = getString(R.string.pregnancy_details)
            pregnancyCard.tvDiagnosis.apply {
                text = getString(R.string.edit_details)
                setTextColor(getColor(requireContext(), R.color.medium_blue))
                typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
            }
            pregnancyCard.tvDiagnosisConfirm.invisible()

            weightCard.tvDiagnosisLbl.text = getString(R.string.weight)
            weightCard.tvDiagnosis.text = hyphen
            weightCard.tvDiagnosisConfirm.invisible()

            eddCard.tvDiagnosisLbl.text =
                getString(R.string.estimated_delivery_date)
            eddCard.tvDiagnosis.text = hyphen
            eddCard.tvDiagnosisConfirm.text = hyphen

            patientStatusCard.tvDiagnosisLbl.text = getString(R.string.patient_status)
            patientStatusCard.tvDiagnosis.apply {
                text = getString(R.string.edit_details)
                setTextColor(getColor(requireContext(), R.color.medium_blue))
                typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
            }
            patientStatusCard.tvDiagnosisConfirm.invisible()
            withNetworkAvailability(online = {
                diagnosisCard.tvDiagnosisConfirm.safeClickListener(this@NCDMedicalReviewDiagnosisCardFragment)
                patientStatusCard.tvDiagnosis.safeClickListener(this@NCDMedicalReviewDiagnosisCardFragment)
            })
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.diagnosisCard.tvDiagnosisConfirm.id -> {
                patientDetailViewModel.getPatientId()?.let {
                    NCDDiagnosisDialogFragment.newInstance(it, getTypeForDiagnoses(),patientDetailViewModel.getGenderIsFemale()).apply {
                        listener = this@NCDMedicalReviewDiagnosisCardFragment
                    }.show(childFragmentManager, NCDDiagnosisDialogFragment.TAG)
                }
            }
            binding.patientStatusCard.tvDiagnosis.id ->{
                patientDetailViewModel.getPatientId()?.let {
                    NCDPatientHistoryDialog.newInstance(
                        it,
                        patientDetailViewModel.getPatientFHIRId(),
                        isFemale = patientDetailViewModel.getGenderIsFemale()
                    ).apply {
                        listener = this@NCDMedicalReviewDiagnosisCardFragment
                    }.show(childFragmentManager, NCDPatientHistoryDialog.TAG)
                }
            }
        }
    }

    override fun onDialogDismissed() {
        // call the get method
    }
}