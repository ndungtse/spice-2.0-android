package com.medtroniclabs.spice.ui.medicalreview.tb.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityTbMedicalReviewBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.SystemicExaminationsFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.ClinicalNotesViewModel
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.SystemicExaminationViewModel
import com.medtroniclabs.spice.ui.medicalreview.investigation.InvestigationActivity
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.prescription.PrescriptionActivity
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.ComorbiditiesFragment
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.PresumptiveTreatmentAndHistoryFragment
import com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel.ComorbiditiesViewModel
import com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel.TbViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.respiratory
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TBMedicalReviewActivity : BaseActivity(), View.OnClickListener, AncVisitCallBack {

    private lateinit var binding: ActivityTbMedicalReviewBinding
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val viewModel: TbViewModel by viewModels()
    private val clinicalNotesViewModel: ClinicalNotesViewModel by viewModels()
    private val presentingComplaintsViewModel: PresentingComplaintsViewModel by viewModels()
    private val systemicExaminationViewModel: SystemicExaminationViewModel by viewModels()
    private val comorbiditiesViewModel: ComorbiditiesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityTbMedicalReviewBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review),
            homeAndBackVisibility = Pair(true, true),
            callback = {
                finish()
            },
            callbackHome = {
                startActivityWithoutSplashScreen()
            }
        )
        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
        viewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
        initStaticDataCall()
        setButtonClickListener()
        attachObserver()
    }

    private fun attachObserver() {
        viewModel.tbMetaResponse.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    initView()
                }

                ResourceState.ERROR -> {
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {
                        if (it) {
                            onBackPressPopStack()
                        }
                    }
                }
            }
        }
        patientViewModel.patientDetailsLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    if (binding.refreshLayout.isRefreshing) {
                        binding.refreshLayout.isRefreshing = false
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {
                        if (it) {
                            onBackPressPopStack()
                        }
                    }
                }
            }
        }
    }

    private fun onBackPressPopStack() {
        this@TBMedicalReviewActivity.finish()
    }

    private fun initStaticDataCall() {
        if (!SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_TB_LOADED.name)) {
            viewModel.getTbStaticData()
        } else {
            initView()
        }
    }

    private fun initView() {
        initializePatientDetailFragment()
    }

    private fun initializePatientDetailFragment() {
        replaceFragment(
            R.id.patientDetailFragment,
            PatientInfoFragment.TAG,
            PatientInfoFragment.newInstance(
                intent.getStringExtra(DefinedParams.PatientId),
                isTb = true
            ).apply {
                setDataCallback(this@TBMedicalReviewActivity)
            }
        )
    }


    private fun swipeRefresh() {
        withNetworkAvailability(online = {
            supportFragmentManager.findFragmentById(R.id.patientDetailFragment)
                .let {
                    patientViewModel.getPatientId()?.let { id ->
                        patientViewModel.getPatients(id)
                    }
                }
        }, offline = {
            if (binding.refreshLayout.isRefreshing) {
                binding.refreshLayout.isRefreshing = false
            }
        })
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.ivPrescription.id -> openPrescriptionActivity()
            binding.ivInvestigation.id -> openInvestigationActivity()
            binding.loadingProgress.id -> {}
            binding.btnSubmit.id -> clickSubmit()
        }
    }
    private fun clickSubmit() {

    }
    private fun openPrescriptionActivity() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { data ->
            val intent = Intent(this, PrescriptionActivity::class.java)
            intent.putExtra(DefinedParams.PatientId, data.patientId)
            intent.putExtra(DefinedParams.EncounterId, patientViewModel.encounterId)
            getResult.launch(intent)
        }
    }

    private fun openInvestigationActivity() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { data ->
            val intent = Intent(this, InvestigationActivity::class.java)
            intent.putExtra(DefinedParams.PatientId, data.patientId)
            intent.putExtra(DefinedParams.EncounterId, patientViewModel.encounterId)
            getResult.launch(intent)
        }
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val value = it.data?.getStringExtra(DefinedParams.EncounterId)
                value?.let { valueString ->
                    patientViewModel.encounterId = valueString
                }
            }
        }

    override fun onDataLoaded(details: PatientListRespModel) {
        viewModel.memberId = details.memberId
        replaceWithDiagnosisFragment()
    }

    private fun setButtonClickListener() {
        binding.btnSubmit.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
        binding.btnRefer.safeClickListener(this)
        binding.ivPrescription.safeClickListener(this)
        binding.ivInvestigation.safeClickListener(this)
    }

    private fun replaceWithDiagnosisFragment() {
        addOrReuseFragment(
            R.id.patientSummaryContainer,
            MedicalReviewPatientDiagnosisFragment.TAG,
            MedicalReviewPatientDiagnosisFragment.newInstance(
                isAnc = false,
                isPnc = false,
                isTB = true,
                patientId = intent.getStringExtra(DefinedParams.PatientId),
                memberID = viewModel.memberId,
                id = intent.getStringExtra(DefinedParams.ID)
            )
        )
        replaceFragment(
            R.id.comorbiditiesContainer,
            ComorbiditiesFragment.TAG,
            ComorbiditiesFragment.newInstance()
        )
        replaceFragment(
            R.id.patientMedicalReviewDiagnosis,
            PresumptiveTreatmentAndHistoryFragment.TAG,
            PresumptiveTreatmentAndHistoryFragment.newInstance()
        )
        initializeFragments()
    }

    private fun initializeFragments() {
        with(binding) {
            presentingComplaintsContainer.visible()
            systemicExaminationsContainer.visible()
            clinicalNotesContainer.visible()
        }
        val bundle = Bundle().apply {
            putString(
                MedicalReviewTypeEnums.PresentingComplaints.name,
                MedicalReviewTypeEnums.TB.name
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                MedicalReviewTypeEnums.TB.name
            )
        }
        replaceFragmentOrCreateNewFragment<PresentingComplaintsFragment>(
            binding.presentingComplaintsContainer.id,
            bundle = bundle,
            tag = PresentingComplaintsFragment.TAG
        )
        replaceFragmentOrCreateNewFragment<SystemicExaminationsFragment>(
            binding.systemicExaminationsContainer.id,
            bundle = bundle,
            tag = SystemicExaminationsFragment.TAG
        )
        replaceFragmentOrCreateNewFragment<ClinicalNotesFragment>(
            binding.clinicalNotesContainer.id,
            bundle = bundle,
            tag = ClinicalNotesFragment.TAG
        )
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.PC_ITEM, this) { _, _ ->
                enableSubmitBtn()
            }
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.SE_ITEM, this) { _, _ ->
                enableSubmitBtn()
            }
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.CLINICAL_NOTES, this) { _, _ ->
                enableSubmitBtn()
            }
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.CF_ITEM, this) { _, _ ->
                enableSubmitBtn()
            }
    }

    private fun enableSubmitBtn() {
        val comorbiditiesFragment =
            supportFragmentManager.findFragmentByTag(ComorbiditiesFragment.TAG) as? ComorbiditiesFragment
        val isValidFragment = comorbiditiesFragment?.validateInput(true)?.first ?: false

        val hasClinicalNotes = clinicalNotesViewModel.enteredClinicalNotes.isNotBlank()
        val hasSelectedPresentingComplaints =
            presentingComplaintsViewModel.selectedPresentingComplaints.isNotEmpty()
        val hasEnteredComplaintNotes =
            presentingComplaintsViewModel.enteredComplaintNotes.isNotBlank()
        val hasEnteredExaminationNotes =
            systemicExaminationViewModel.enteredExaminationNotes.isNotBlank()

        val hasSelectedSystemicExaminations =
            systemicExaminationViewModel.selectedSystemicExaminations.isNotEmpty()
        val isRespiratorySelected =
            systemicExaminationViewModel.selectedSystemicExaminations.any { it.value == respiratory }
        val hasValidRespiratoryNotes =
            !systemicExaminationViewModel.respiratoryNotes.isNullOrBlank()
        val hasComorbidities = comorbiditiesViewModel.chips.isNotEmpty()
        // If "Ear" is selected, respiratoryNotes must not be blank
        val isSystemicExaminationValid = !isRespiratorySelected || hasValidRespiratoryNotes

        binding.btnSubmit.isEnabled = (hasClinicalNotes ||
                hasSelectedPresentingComplaints ||
                hasEnteredComplaintNotes ||
                hasEnteredExaminationNotes ||
                hasSelectedPresentingComplaints ||
                isValidFragment)

        if (hasSelectedSystemicExaminations) {
            binding.btnSubmit.isEnabled = isSystemicExaminationValid
            if (!isSystemicExaminationValid) {
                return
            }
        }
        if (hasComorbidities) {
            binding.btnSubmit.isEnabled = isValidFragment
            return
        }
    }
}