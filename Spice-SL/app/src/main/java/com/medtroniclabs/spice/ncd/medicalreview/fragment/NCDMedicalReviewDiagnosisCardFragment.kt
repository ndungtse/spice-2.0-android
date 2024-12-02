package com.medtroniclabs.spice.ncd.medicalreview.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.common.CommonUtils.convertListToString
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.FragmentNcdMedicalReviewDiagnosisCardBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.assessment.dialog.BPLogListDialog
import com.medtroniclabs.spice.ncd.assessment.dialog.GlucoseLogListDialog
import com.medtroniclabs.spice.ncd.assessment.ui.AssessmentReadingActivity
import com.medtroniclabs.spice.ncd.assessment.viewmodel.BloodPressureViewModel
import com.medtroniclabs.spice.ncd.assessment.viewmodel.GlucoseViewModel
import com.medtroniclabs.spice.ncd.data.NCDDiagnosisGetRequest
import com.medtroniclabs.spice.ncd.medicalreview.NCDDialogDismissListener
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.IS_FEMALE
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.IS_INITIAL_MR
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.MENU_ID
import com.medtroniclabs.spice.ncd.medicalreview.NCDMedicalReviewActivity
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDDiagnosisDialogFragment
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDMentalHealthQuestionDialog
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDPatientHistoryDialog
import com.medtroniclabs.spice.ncd.medicalreview.dialog.NCDPregnancyDialog
import com.medtroniclabs.spice.ncd.medicalreview.viewmodel.NCDMedicalReviewDiagnosisCardViewModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.BaseFragment
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.MotherNeonateUtil
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

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
                        val diag = data.diagnosis?.mapNotNull { it.name }
                            ?.let { convertListToString(ArrayList(it)) }
                        binding.diagnosisCard.tvDiagnosis.text = diag ?: getString(R.string.hyphen_symbol)
                        binding.diagnosisCard.tvDiagnosisConfirm.text =
                            if (diag.isNullOrBlank())
                                getString(R.string.confirm_diagnoses)
                            else
                                getString(R.string.edit_diagnosis)
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
            val isFemalePregnancy = isFemalePregnancy()
            val isContinuous = getInitialMr()
            val isMaternal = !getInitialMr() && isFemalePregnancy
            val isNCDAndMentalHealth = !getInitialMr()

            // Hide all cards initially
            pregnancyCard.root.setVisible(false)
            weightCard.root.setVisible(false)
            eddCard.root.setVisible(false)
            patientStatusCard.root.setVisible(false)
            mentalHealthCard.root.setVisible(false)
            when {
                isContinuous -> {
                    pregnancyCard.root.setVisible(isFemalePregnancy)
                    weightCard.root.setVisible(isFemalePregnancy)
                    eddCard.root.setVisible(isFemalePregnancy)
                }

                isMaternal -> {
                    pregnancyCard.root.setVisible(true)
                    weightCard.root.setVisible(true)
                    eddCard.root.setVisible(true)
                }

                isNCDAndMentalHealth -> {
                    if (getMenu().equals(NCDMRUtil.NCD, true)) {
                        // restrict the patient status hide
                        patientStatusCard.root.setVisible(false)
                    } else if (getMenu().equals(NCDMRUtil.MENTAL_HEALTH, true)) {
                        mentalHealthCard.root.setVisible(true)
                        patientStatusCard.root.setVisible(false)
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

            val latestGlucose = patientDetailViewModel.recentGlucose()
            bgCard.tvDiagnosisLbl.text = getString(R.string.blood_glucose)
            bgCard.tvDiagnosis.text = latestGlucose.ifBlank { hyphen }
            bgCard.tvDiagnosisConfirm.text =
                if (latestGlucose.isBlank()) getString(R.string.add_new_reading) else getString(R.string.view_details)

            pregnancyCard.tvDiagnosisLbl.text = getString(R.string.pregnancy_details)
            pregnancyCard.tvDiagnosis.apply {
                text = getString(R.string.edit_details)
                setTextColor(getColor(requireContext(), R.color.medium_blue))
                typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
            }
            pregnancyCard.tvDiagnosisConfirm.invisible()

            weightCard.tvDiagnosisLbl.text = getString(R.string.weight)
            weightCard.tvDiagnosis.text = MotherNeonateUtil.convertWeight(
                patientDetailViewModel.getWeightInKG(),
                requireContext()
            )
            weightCard.tvDiagnosisConfirm.invisible()

            val pregnantDetails = patientDetailViewModel.getPregnantDetails()
            eddCard.tvDiagnosisLbl.text =
                getString(R.string.estimated_delivery_date)
            eddCard.tvDiagnosis.text =
                pregnantDetails?.estimatedDeliveryDate?.let {
                    DateUtils.convertDateFormat(
                        it,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                        DateUtils.DATE_ddMMyyyy
                    ).takeIf { it.isNotBlank() } ?: hyphen
                } ?: hyphen
            eddCard.tvDiagnosisConfirm.text =
                pregnantDetails?.gestationalAge?.toLongOrNull()?.let { gestationalAge ->
                    DateUtils.formatGestationalAge(gestationalAge, requireContext())
                } ?: hyphen
            eddCard.tvDiagnosisConfirm.apply {
                setTextColor(getColor(requireContext(), R.color.text_label_color))
                typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
            }
            patientStatusCard.tvDiagnosisLbl.text = getString(R.string.patient_status)
            patientStatusCard.tvDiagnosis.apply {
                text = getString(R.string.edit_details)
                setTextColor(getColor(requireContext(), R.color.medium_blue))
                typeface = ResourcesCompat.getFont(requireContext(), R.font.inter_regular)
            }
            patientStatusCard.tvDiagnosisConfirm.invisible()
            bpCard.tvDiagnosisConfirm.safeClickListener(this@NCDMedicalReviewDiagnosisCardFragment)
            bgCard.tvDiagnosisConfirm.safeClickListener(this@NCDMedicalReviewDiagnosisCardFragment)
            pregnancyCard.tvDiagnosis.safeClickListener(this@NCDMedicalReviewDiagnosisCardFragment)
            diagnosisCard.tvDiagnosisConfirm.safeClickListener(this@NCDMedicalReviewDiagnosisCardFragment)
            patientStatusCard.tvDiagnosis.safeClickListener(this@NCDMedicalReviewDiagnosisCardFragment)
            mentalHealthCard.tvAssessmentPHQ9.safeClickListener(this@NCDMedicalReviewDiagnosisCardFragment)
            mentalHealthCard.tvAssessmentGAD7.safeClickListener(this@NCDMedicalReviewDiagnosisCardFragment)
        }
    }

    private fun isFemalePregnancy(): Boolean {
        return getMenu().equals(DefinedParams.PregnancyANC, true) && getIsFemale()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.diagnosisCard.tvDiagnosisConfirm -> {
                withNetworkAvailability(online = {
                    showDiagnosisDialog()
                })
            }

            binding.patientStatusCard.tvDiagnosis -> {
                withNetworkAvailability(online = {
                    showPatientHistoryDialog()
                })
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
                withNetworkAvailability(online = {
                    patientDetailViewModel.getPatientFHIRId()?.let { id ->
                        val dialog = childFragmentManager.findFragmentByTag(NCDPregnancyDialog.TAG)
                        if (dialog == null) {
                            val ncdPregnancyDialog =
                                NCDPregnancyDialog.newInstance(
                                    patientId = id,
                                    patientDetailViewModel.getGenderIsFemale(),
                                    patientDetailViewModel.isPregnant()
                                ) { isPositiveResult, message ->
                                    if (isPositiveResult) {
                                        showSuccessDialogue(
                                            title = getString(R.string.pregnancy_details),
                                            message = message,
                                        )
                                        (requireActivity() as? NCDMedicalReviewActivity)?.swipeRefresh()
                                    } else showErrorDialog(
                                        title = getString(R.string.error),
                                        message = message
                                    )
                                }
                            ncdPregnancyDialog.show(childFragmentManager, NCDPregnancyDialog.TAG)
                        }
                    }
                })
            }

            binding.bgCard.tvDiagnosisConfirm -> {
                withNetworkAvailability(online = {
                    if (patientDetailViewModel.recentGlucose().isBlank())
                        addNewReading(false)
                    else
                        patientDetailViewModel.getPatientFHIRId()?.let { id ->
                            glucoseViewModel.glucoseLogList(patientId = id)
                        }
                })
            }

            binding.mentalHealthCard.tvAssessmentPHQ9 -> {
                patientDetailViewModel.patientDetailsLiveData.value?.data?.let {
                    withNetworkAvailability(online = {
                        showMentalHealthDialog(AssessmentDefinedParams.phq9, it.phq4score != null)
                    })
                }
            }

            binding.mentalHealthCard.tvAssessmentGAD7 -> {
                patientDetailViewModel.patientDetailsLiveData.value?.data?.let {
                    withNetworkAvailability(online = {
                        showMentalHealthDialog(AssessmentDefinedParams.gad7, it.gad7Score != null)
                    })
                }
            }
        }
    }

    private fun showDiagnosisDialog() {
        val dialog = childFragmentManager.findFragmentByTag(NCDDiagnosisDialogFragment.TAG)
        if (dialog == null) {
            NCDDiagnosisDialogFragment.newInstance(
                patientDetailViewModel.getPatientId(),
                patientDetailViewModel.getPatientFHIRId(),
                NCDMRUtil.getTypeForDiagnoses(getMenu()),
                patientDetailViewModel.getGenderIsFemale(),
                NCDMRUtil.getConfirmDiagnoses(getMenu()),
                patientDetailViewModel.isPregnant(),
                getMenu()
            ).apply {
                listener = this@NCDMedicalReviewDiagnosisCardFragment
            }.show(childFragmentManager, NCDDiagnosisDialogFragment.TAG)
        }
    }

    private fun showPatientHistoryDialog() {
        val dialog = childFragmentManager.findFragmentByTag(NCDPatientHistoryDialog.TAG)
        if (dialog == null) {
            patientDetailViewModel.getPatientId()?.let {
                NCDPatientHistoryDialog.newInstance(
                    it,
                    patientDetailViewModel.getPatientFHIRId(),
                    isFemale = patientDetailViewModel.getGenderIsFemale(),
                    isPregnant = patientDetailViewModel.isPregnant()
                ).apply {
                    listener = this@NCDMedicalReviewDiagnosisCardFragment
                }.show(childFragmentManager, NCDPatientHistoryDialog.TAG)
            }
        }
    }

    private fun showMentalHealthDialog(type: String, isEditAssessment: Boolean) {
        val dialog = childFragmentManager.findFragmentByTag(NCDMentalHealthQuestionDialog.TAG)
        if (dialog == null) {
            NCDMentalHealthQuestionDialog.newInstance(
                type,
                patientDetailViewModel.getPatientId(),
                patientDetailViewModel.getPatientFHIRId(),
                isEditAssessment
            )
                .show(childFragmentManager, NCDPatientHistoryDialog.TAG)
        }
    }

    override fun onDialogDismissed(isConfirmed: Boolean) {
        // call the get method
        if (isConfirmed) {
            withNetworkAvailability(online = {
                getDiagonsis()
            })
        } else {
            //patient status for NCD
            // now it is hide so it is not used now
            val dialogFragment =
                childFragmentManager.findFragmentByTag(NCDPatientHistoryDialog.TAG) as? NCDPatientHistoryDialog
            dialogFragment?.dismiss()
            (requireActivity() as? NCDMedicalReviewActivity)?.showCurrentMedication()
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
            intent.putExtra(
                DefinedParams.FORM_TYPE_ID,
                if (isBP) DefinedParams.BP_LOG else DefinedParams.GLUCOSE_LOG
            )
            intent.putExtra(DefinedParams.IntentPatientDetails, detail)
            getResult.launch(intent)
        }
    }
    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                (requireActivity() as? NCDMedicalReviewActivity)?.swipeRefresh()
            }
        }
}