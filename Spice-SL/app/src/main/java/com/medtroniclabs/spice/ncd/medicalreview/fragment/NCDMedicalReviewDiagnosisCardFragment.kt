package com.medtroniclabs.spice.ncd.medicalreview.fragment

import android.content.Intent
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
import com.medtroniclabs.spice.common.CommonUtils.convertListToString
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentNcdMedicalReviewDiagnosisCardBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisGetRequest
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.ncd.assessment.dialog.BPLogListDialog
import com.medtroniclabs.spice.ncd.assessment.dialog.GlucoseLogListDialog
import com.medtroniclabs.spice.ncd.assessment.ui.AssessmentReadingActivity
import com.medtroniclabs.spice.ncd.assessment.viewmodel.BloodPressureViewModel
import com.medtroniclabs.spice.ncd.assessment.viewmodel.GlucoseViewModel
import com.medtroniclabs.spice.ncd.medicalreview.NCDDialogDismissListener
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.IS_FEMALE
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.IS_INITIAL_MR
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.MENU_ID
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDDiagnosisDialogFragment
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDPatientHistoryDialog
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDPregnancyDialog
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewDiagnosisCardViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.ArrayList

@AndroidEntryPoint
class NCDMedicalReviewDiagnosisCardFragment : BaseFragment(), View.OnClickListener,
    NCDDialogDismissListener {
    private val patientDetailViewModel: PatientDetailViewModel by activityViewModels()
    private val medicalReviewDiagnosisCardViewModel: NCDMedicalReviewDiagnosisCardViewModel by activityViewModels()
    private lateinit var binding: FragmentNcdMedicalReviewDiagnosisCardBinding
    private val bpViewModel: BloodPressureViewModel by activityViewModels()
    private val glucoseViewModel: GlucoseViewModel by activityViewModels()
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
        attachObservers()
    }

    private fun attachObservers() {
        medicalReviewDiagnosisCardViewModel.getConfirmDiagonsis.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {

                }

                ResourceState.SUCCESS -> {
                    resourceState.data?.let { data ->
                        binding.diagnosisCard.tvDiagnosis.text = data.diagnosis
                            ?.mapNotNull { it.name }
                            ?.let { convertListToString(ArrayList(it)) }
                            ?: getString(R.string.hyphen_symbol)
                    }
                }

                ResourceState.ERROR -> {

                }
            }
        }
        bpViewModel.bpLogListResponseLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as? BaseActivity?)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as? BaseActivity?)?.hideLoading()
                    val bpLogListDialog = BPLogListDialog.newInstance { addNewReading(true) }
                    bpLogListDialog.show(childFragmentManager, BPLogListDialog.TAG)
                }

                ResourceState.ERROR -> {
                    (activity as? BaseActivity?)?.hideLoading()
                    resourceState.message?.let { message ->
                        (activity as? BaseActivity?)?.showErrorDialogue(message = message) {}
                    }
                }
            }
        }
        glucoseViewModel.glucoseLogListResponseLiveData.observe(viewLifecycleOwner) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as? BaseActivity?)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as? BaseActivity?)?.hideLoading()
                    val glucoseLogDialog = GlucoseLogListDialog.newInstance { addNewReading(false) }
                    glucoseLogDialog.show(childFragmentManager, GlucoseLogListDialog.TAG)
                }

                ResourceState.ERROR -> {
                    (activity as? BaseActivity?)?.hideLoading()
                    resourceState.message?.let { message ->
                        (activity as? BaseActivity?)?.showErrorDialogue(message = message) {}
                    }
                }
            }
        }
    }

    private fun getInitialMr(): Boolean {
        return arguments?.getBoolean(IS_INITIAL_MR) ?: false
    }

    private fun getIsFemale(): Boolean {
        return arguments?.getBoolean(IS_FEMALE) ?: false
    }

    private fun getMenu(): String? {
        return arguments?.getString(MENU_ID)?.lowercase()
    }

    private fun initView() {
        getDiagonsis()
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

            val latestBP = patientDetailViewModel.recentBP()
            bpCard.tvDiagnosisLbl.text = getString(R.string.blood_pressure)
            bpCard.tvDiagnosis.text = latestBP.ifBlank { hyphen }
            bpCard.tvDiagnosisConfirm.text =
                if (latestBP.isBlank()) getString(R.string.add_new_reading) else getString(R.string.view_details)
            bpCard.tvDiagnosisConfirm.safeClickListener(this@NCDMedicalReviewDiagnosisCardFragment)

            val latestGlucose = patientDetailViewModel.recentGlucose()
            bgCard.tvDiagnosisLbl.text = getString(R.string.blood_glucose)
            bgCard.tvDiagnosis.text = latestGlucose.ifBlank { hyphen }
            bgCard.tvDiagnosisConfirm.text =
                if (latestGlucose.isBlank()) getString(R.string.add_new_reading) else getString(R.string.view_details)
            bgCard.tvDiagnosisConfirm.safeClickListener(this@NCDMedicalReviewDiagnosisCardFragment)

            pregnancyCard.tvDiagnosisLbl.text = getString(R.string.pregnancy_details)
            pregnancyCard.tvDiagnosis.apply {
                text = getString(R.string.edit_details)
                setTextColor(getColor(requireContext(), R.color.medium_blue))
                typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
            }
            pregnancyCard.tvDiagnosis.safeClickListener(this@NCDMedicalReviewDiagnosisCardFragment)
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
        when (v) {
            binding.diagnosisCard.tvDiagnosisConfirm -> {
                showDiagnosisDialog()
            }
            binding.patientStatusCard.tvDiagnosis ->{
                showPatientHistoryDialog()
            }
            binding.bpCard.tvDiagnosisConfirm -> {
                if (patientDetailViewModel.recentBP().isBlank())
                    addNewReading(true)
                else
                    patientDetailViewModel.getPatientFHIRId()?.let { id ->
                        bpViewModel.bpLogList(patientId = id)
                    }
            }

            binding.pregnancyCard.tvDiagnosis -> {
                patientDetailViewModel.getPatientFHIRId()?.let { id ->
                    val ncdPregnancyDialog =
                        NCDPregnancyDialog.newInstance(patientId = id) { isPositiveResult, message ->
                            if (isPositiveResult) showSuccessDialogue(
                                title = getString(R.string.pregnancy_details),
                                message = message,
                            )
                            else showErrorDialog(
                                title = getString(R.string.error),
                                message = message
                            )
                        }
                    ncdPregnancyDialog.show(childFragmentManager, NCDPregnancyDialog.TAG)
                }
            }
            binding.bgCard.tvDiagnosisConfirm -> {
                if (patientDetailViewModel.recentGlucose().isBlank())
                    addNewReading(false)
                else
                    patientDetailViewModel.getPatientFHIRId()?.let { id ->
                        glucoseViewModel.glucoseLogList(patientId = id)
                    }
            }

        }
    }

    private fun showDiagnosisDialog() {
        val dialog = childFragmentManager.findFragmentByTag(NCDDiagnosisDialogFragment.TAG)
        if (dialog == null) {
            patientDetailViewModel.getPatientId()?.let {
                NCDDiagnosisDialogFragment.newInstance(
                    it,
                    NCDMRUtil.getTypeForDiagnoses(getMenu()),
                    patientDetailViewModel.getGenderIsFemale(),
                    NCDMRUtil.getConfirmDiagnoses(getMenu())
                ).apply {
                    listener = this@NCDMedicalReviewDiagnosisCardFragment
                }.show(childFragmentManager, NCDDiagnosisDialogFragment.TAG)
            }
        }
    }

    private fun showPatientHistoryDialog() {
        val dialog = childFragmentManager.findFragmentByTag(NCDPatientHistoryDialog.TAG)
        if (dialog == null) {
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
    override fun onDialogDismissed(isConfirmed: Boolean) {
        // call the get method
        if (isConfirmed) {
            getDiagonsis()
        }
    }

    private fun getDiagonsis() {
        patientDetailViewModel.getPatientId()?.let { patientId ->
            medicalReviewDiagnosisCardViewModel.getConfirmDiagonsis(
                NCDDiagnosisGetRequest(
                    patientReference = patientId,
                    diagnosisType = NCDMRUtil.getConfirmDiagnoses(getMenu())
                )
            )
        }
    }

    private fun addNewReading(isBP: Boolean) {
        patientDetailViewModel.patientDetailsLiveData.value?.data?.let { detail ->
            val intent = Intent(requireActivity(), AssessmentReadingActivity::class.java)
            val bundle = Bundle().apply {
                if (isBP)
                    putString(DefinedParams.FORM_TYPE_ID, DefinedParams.BP_LOG)
                else
                    putString(DefinedParams.FORM_TYPE_ID, DefinedParams.GLUCOSE_LOG)

                putString(DefinedParams.RelatedPersonFhirId, detail.id)
                putString(DefinedParams.PATIENT_ID, detail.patientId)

                putBoolean(Screening.is_regular_smoker, detail.isRegularSmoker ?: false)
                putString(Screening.DateOfBirth, detail.dateOfBirth)
                putString(DefinedParams.Gender, detail.gender)
            }
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }
}