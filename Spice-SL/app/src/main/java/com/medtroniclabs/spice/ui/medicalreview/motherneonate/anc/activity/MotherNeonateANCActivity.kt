package com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.isNotTabletAndPortrait
import com.medtroniclabs.spice.appextensions.setPercentWidth
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.PregnancyANC
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.data.AboveFiveYearsSummarySubmitRequest
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.ActivityMedicalReviewAncactivityBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.dialog.MedicalReviewSuccessDialogFragment
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.SystemicExaminationsFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.ClinicalNotesViewModel
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.SystemicExaminationViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.MotherNeonateAncHistoryFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.MotherNeonateAncSummary
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.PregnancyDetailsFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.PregnancyPastObstetricHistoryFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.PregnancySummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.viewmodel.MotherNeonateANCViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.viewmodel.MotherNeonateSummaryViewModel
import com.medtroniclabs.spice.ui.medicalreview.prescription.PrescriptionActivity
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.ReferPatientFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PregnancyDetailsViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PregnancyPastObstetricHistoryViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.ReferPatientViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MotherNeonateANCActivity : BaseActivity(), View.OnClickListener, AncVisitCallBack,
    OnDialogDismissListener {

    private lateinit var binding: ActivityMedicalReviewAncactivityBinding
    private val viewModel: MotherNeonateANCViewModel by viewModels()
    private val presentingComplaintsViewModel: PresentingComplaintsViewModel by viewModels()
    private val systemicExaminationViewModel: SystemicExaminationViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val clinicalNotesViewModel: ClinicalNotesViewModel by viewModels()
    private val pregnancyPastObstetricHistoryViewModel: PregnancyPastObstetricHistoryViewModel by viewModels()
    private val pregnancyDetailsViewModel: PregnancyDetailsViewModel by viewModels()
    private val motherNeonateSummaryViewModel: MotherNeonateSummaryViewModel by viewModels()
    private val referPatientViewModel: ReferPatientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityMedicalReviewAncactivityBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review),
            callback = {
                backNavigation()
            }
        )
        attachObservers()
        getCurrentLocation()
        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
        viewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
        initStaticDataCall()
        setButtonClickListener()
    }

    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(this)
        locationManager.getCurrentLocation {
            viewModel.lastLocation = it
        }
    }

    private fun initStaticDataCall() {
        if (!SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_MOTHER_NEONATE_LOADEDANC.name)) {
            viewModel.getMotherNeoNateAncStaticData()
        } else {
            initView()
        }
    }

    private fun attachObservers() {
        viewModel.summaryCreateResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    MedicalReviewSuccessDialogFragment.newInstance().show(
                        supportFragmentManager,
                        MedicalReviewSuccessDialogFragment.TAG
                    )
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

        viewModel.motherNeonateMetaResponse.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
//                    hideLoading()
                    initView()
                }

                ResourceState.ERROR -> {
//                    hideLoading()
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


        viewModel.motherNeonateCreateResponse.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let {
                        binding.loadingProgress.visible()
                        handleSummary(it.encounterId)
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }

        referPatientViewModel.referPatientResultLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    val fragment =
                        supportFragmentManager.findFragmentByTag(ReferPatientFragment.TAG) as? ReferPatientFragment
                    fragment?.dismiss()
                    MedicalReviewSuccessDialogFragment.newInstance().show(
                        supportFragmentManager,
                        MedicalReviewSuccessDialogFragment.TAG
                    )
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
        pregnancyPastObstetricHistoryViewModel.checkSubmitBtn.observe(this) {
            isPregnancyDetailsAndHistoryValidation()
        }
        pregnancyDetailsViewModel.checkSubmitBtn.observe(this) {
            isPregnancyDetailsAndHistoryValidation()
        }
        motherNeonateSummaryViewModel.checkSubmitBtn.observe(this) {
            isMotherNeonateSummaryValidation()
        }
    }
    private fun isMotherNeonateSummaryValidation() {
        binding.btnDone.isEnabled =
            motherNeonateSummaryViewModel.nextFollowupDate?.isNotBlank() == true || motherNeonateSummaryViewModel.patientStatus?.isNotBlank() == true
    }
    private fun isPregnancyDetailsAndHistoryValidation() {
        val model = pregnancyDetailsViewModel.pregnancyDetailsModel
        binding.btnLayout.btnNext.isEnabled =
            pregnancyPastObstetricHistoryViewModel.pregnancyHistoryNotes?.isNotBlank() == true || pregnancyPastObstetricHistoryViewModel.pregnancyHistoryChip.isNotEmpty() || pregnancyPastObstetricHistoryViewModel.resultFlowHashMap[PregnancyPastObstetricHistoryFragment.TAG] != null || model.height != null ||
                    model.weight != null ||
                    model.pulse != null ||
                    model.lastMenstrualPeriod != null ||
                    model.estimatedDeliveryDate != null ||
                    model.noOfFetus != null ||
                    model.gravida != null ||
                    model.parity != null ||
                    model.patientBloodGroup != null ||
                    model.systolic != null ||
                    model.diastolic != null
    }

    private fun swipeRefresh() {
        supportFragmentManager.findFragmentById(R.id.patientDetailFragment)
            .let {
                patientViewModel.getPatientId()?.let { id ->
                    patientViewModel.getPatients(id)
                }
            }
    }

    private fun initView() {
        initializePatientDetailFragment()
    }

    private fun initializePatientDetailFragment() {
        val fragmentManager = supportFragmentManager
        val existingFragment = fragmentManager.findFragmentById(R.id.patientDetailFragment)
        if (existingFragment == null) {
            val fragment =
                PatientInfoFragment.newInstance(
                    intent.getStringExtra(DefinedParams.PatientId),
                    isAnc = true
                )
            fragment.setDataCallback(this)
            fragmentManager.beginTransaction()
                .add(R.id.patientDetailFragment, fragment)
                .commit()
        } else if (existingFragment !is PatientInfoFragment) {
            val fragment =
                PatientInfoFragment.newInstance(
                    intent.getStringExtra(DefinedParams.PatientId),
                    isAnc = true
                )
            fragment.setDataCallback(this)
            fragmentManager.beginTransaction()
                .replace(R.id.patientDetailFragment, fragment)
                .commit()
        }
    }

    private fun initializePregnancyDetailsFragment() {
        val fragmentManager = supportFragmentManager
        val existingFragment = fragmentManager.findFragmentById(R.id.pregnancyDetailsConatiner)

        if (existingFragment == null) {
            fragmentManager.beginTransaction()
                .add(R.id.pregnancyDetailsConatiner, PregnancyDetailsFragment.newInstance())
                .commit()
        } else if (existingFragment !is PregnancyDetailsFragment) {
            fragmentManager.beginTransaction()
                .replace(R.id.pregnancyDetailsConatiner, PregnancyDetailsFragment.newInstance())
                .commit()
        }
    }

    private fun initializePregnancyHistoryFragment() {
        val fragmentManager = supportFragmentManager
        val existingFragment = fragmentManager.findFragmentById(R.id.pregnancyHistoryConatiner)

        if (existingFragment == null) {
            fragmentManager.beginTransaction()
                .add(
                    R.id.pregnancyHistoryConatiner,
                    PregnancyPastObstetricHistoryFragment.newInstance()
                )
                .commit()
        } else if (existingFragment !is PregnancyPastObstetricHistoryFragment) {
            fragmentManager.beginTransaction()
                .replace(
                    R.id.pregnancyHistoryConatiner,
                    PregnancyPastObstetricHistoryFragment.newInstance()
                )
                .commit()
        }
    }


    private fun hideContainers() {
        with(binding) {
            presentingComplaintsContainer.gone()
            systemicExaminationsContainer.gone()
            clinicalNotesContainer.gone()
            bottomNavigationView.gone()
            btnLayout.btnNext.visible()
        }
    }

    private fun setButtonClickListener() {
        binding.btnLayout.btnNext.safeClickListener(this)
        binding.btnSubmit.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
        binding.btnRefer.safeClickListener(this)
        binding.ivPrescription.safeClickListener(this)
    }

    private fun setButtonWidth() {
        val width = if (isNotTabletAndPortrait()) 0.4f else 0.2f
        binding.btnLayout.root.setPercentWidth(binding.btnLayout.btnNext.id, width)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnLayout.btnNext.id -> {
                validatePregnantDetails()
            }

            binding.ivPrescription.id -> {
                patientViewModel.patientDetailsLiveData.value?.data?.let {data ->
                    val intent  = Intent(this, PrescriptionActivity::class.java)
                    intent.putExtra(DefinedParams.PatientId,data.patientId)
                    startActivity(intent)
                }
            }

            binding.btnSubmit.id -> {
                val fragment =
                    supportFragmentManager.findFragmentById(R.id.systemicExaminationsContainer) as? SystemicExaminationsFragment
                val clFragment =
                    supportFragmentManager.findFragmentById(R.id.clinicalNotesContainer) as? ClinicalNotesFragment
                if (fragment != null && clFragment != null) {
                    val isFragmentValid = fragment.validateInput()
                    val isClFragmentValid = clFragment.validateInput()

                    if (isFragmentValid && isClFragmentValid) {
                        submitRequest()
                    }
                }
            }

            binding.btnDone.id -> {
                submitSummary()
            }

            binding.btnRefer.id -> {
                viewModel.motherNeonateCreateResponse.value?.data?.let {
                    ReferPatientFragment.newInstance(
                        MedicalReviewTypeEnums.ANC.name,
                        it.patientReference,
                        it.encounterId
                    )
                        .show(
                            supportFragmentManager,
                            ReferPatientFragment.TAG
                        )
                }
            }
        }
    }

    private fun submitSummary() {
        val motherNeonateSummary =
            supportFragmentManager.findFragmentById(R.id.pregnancyDetailsConatiner) as? MotherNeonateAncSummary
        val isValidSummary = motherNeonateSummary?.validateInput() ?: false

        if (isValidSummary) {
            val submitCreateId = viewModel.getSubmitCreateId()
            val nextVisitDate = DateUtils.convertDateTimeToDate(
                motherNeonateSummaryViewModel.nextFollowupDate,
                DateUtils.DATE_ddMMyyyy,
                DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
            )
            val request = AboveFiveYearsSummarySubmitRequest(
                referralTicketType = MedicalReviewTypeEnums.RMNCH.name,
                memberId = patientViewModel.getPatientMemberId(),
                id = submitCreateId,
                provenance = ProvanceDto(
                    createdDateTime = DateUtils.getCurrentDateAndTime(
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    )
                ),
                householdId = patientViewModel.getPatientHouseholdId(),
                patientReference = viewModel.getPatientReference(),
                nextVisitDate = nextVisitDate,
                patientStatus = motherNeonateSummaryViewModel.patientStatus,
            )
            if (connectivityManager.isNetworkAvailable()) {
                viewModel.motherNeonateSummaryCreate(request)
            } else {
                showErrorDialogue(
                    getString(R.string.error), getString(R.string.no_internet_error),
                    isNegativeButtonNeed = false,
                ) {}
            }
        }
    }


    private fun submitRequest() {
        viewModel.motherNeonateAncRequest.apply {
            assessmentType = PregnancyANC
            presentingComplaints =
                presentingComplaintsViewModel.selectedPresentingComplaints.map { it.value }
            presentingComplaintsNotes = presentingComplaintsViewModel.enteredComplaintNotes
            obstetricExaminations =
                systemicExaminationViewModel.selectedSystemicExaminations.map { it.value }
            obstetricExaminationNotes = systemicExaminationViewModel.enteredExaminationNotes
            fundalHeight = systemicExaminationViewModel.fundalHeight
            fetalHeartRate = systemicExaminationViewModel.fetalHeartRate
            clinicalNotes = clinicalNotesViewModel.enteredClinicalNotes
            pregnancyDetails = pregnancyDetailsViewModel.pregnancyDetailsModel
            deliveryKit = pregnancyPastObstetricHistoryViewModel.deliveryKit
            pregnancyHistory = pregnancyPastObstetricHistoryViewModel.pregnancyHistoryChip
                .filter { it.name != DefinedParams.Other }
                .map { it.value}
            pregnancyHistoryNotes = pregnancyPastObstetricHistoryViewModel.pregnancyHistoryNotes
            patientReference = patientViewModel.getPatientFHIRId()
        }
        if (connectivityManager.isNetworkAvailable()) {
            viewModel.createMotherNeonate()
        } else {
            showErrorDialogue(
                getString(R.string.error), getString(R.string.no_internet_error),
                isNegativeButtonNeed = false,
            ) {}
        }
    }

    private fun validatePregnantDetails() {
        val pregnancyDetailsFragment =
            supportFragmentManager.findFragmentById(R.id.pregnancyDetailsConatiner) as? PregnancyDetailsFragment
        if (pregnancyDetailsFragment?.validateInput() == true) {
            binding.loadingProgress.visible()
            handleSubmit()
        }
    }

    private fun backNavigation() {
        val fragmentManager = supportFragmentManager
        val pregnancyDetailsFragment =
            fragmentManager.findFragmentById(R.id.pregnancyDetailsConatiner)
        if (viewModel.ancVisit == 1 && pregnancyDetailsFragment is PregnancyDetailsFragment) {
            // Show the dialog here
            showErrorDialog()
        } else if (viewModel.ancVisit == 1 && pregnancyDetailsFragment is MedicalReviewPatientDiagnosisFragment) {
            binding.loadingProgress.visible()
            initializePregnancyDetailsFragment()
            initializePregnancyHistoryFragment()
            hideContainers()
            setButtonWidth()
            binding.loadingProgress.gone()
        } else if (pregnancyDetailsFragment is MotherNeonateAncSummary) {
            showErrorDialog()
        } else if (viewModel.ancVisit > 1 && pregnancyDetailsFragment is MedicalReviewPatientDiagnosisFragment) {
            showErrorDialog()
        }
    }


    private fun showErrorDialog() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                onBackPressPopStack()
            }
        }
    }

    private fun onBackPressPopStack() {
        this@MotherNeonateANCActivity.finish()
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
                MedicalReviewTypeEnums.ANC.name
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                MedicalReviewTypeEnums.ANC.name
            )
        }
        replaceFragmentInId<PresentingComplaintsFragment>(
            binding.presentingComplaintsContainer.id,
            bundle = bundle,
            tag = PresentingComplaintsFragment::class.simpleName
        )
        replaceFragmentInId<SystemicExaminationsFragment>(
            binding.systemicExaminationsContainer.id,
            bundle = bundle,
            tag = SystemicExaminationsFragment::class.simpleName
        )
        replaceFragmentInId<ClinicalNotesFragment>(
            binding.clinicalNotesContainer.id,
            bundle = bundle,
            tag = ClinicalNotesFragment::class.simpleName
        )
    }


    private fun handleSubmit(isAncVisitOne: Boolean = true) {
        replaceWithDiagnosisFragment()
        if (isAncVisitOne) {
            replaceWithPregnancySummaryFragment()
        } else {
            replaceWithMotherNeonateAncHistoryFragment()
        }
        scrollToTop()
        hideNextButton()
        showBottomNavigation()
        initializeFragments()
        attachObserversListenerForChip()
        binding.loadingProgress.gone()
    }

    private fun attachObserversListenerForChip() {
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
    }

    private fun enableSubmitBtn() {
        binding.btnSubmit.isEnabled = clinicalNotesViewModel.enteredClinicalNotes.isNotBlank() ||
                (presentingComplaintsViewModel.selectedPresentingComplaints.isNotEmpty() || systemicExaminationViewModel.selectedSystemicExaminations.isNotEmpty() || presentingComplaintsViewModel.enteredComplaintNotes.isNotBlank() || systemicExaminationViewModel.enteredExaminationNotes.isNotBlank() || systemicExaminationViewModel.fundalHeight != null || systemicExaminationViewModel.fetalHeartRate != null)
    }

    private fun replaceWithDiagnosisFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.pregnancyDetailsConatiner,
                MedicalReviewPatientDiagnosisFragment.newInstance(
                    true,
                    intent.getStringExtra(DefinedParams.PatientId),
                    viewModel.memberId,
                    intent.getStringExtra(DefinedParams.ID)
                )
            )
            .commit()
    }

    private fun replaceWithPregnancySummaryFragment() {
        val fragment = PregnancySummaryFragment.newInstance()
        fragment.setData(
            pregnancyPastObstetricHistoryViewModel.pregnancyHistoryChip,
            pregnancyPastObstetricHistoryViewModel.pregnancyHistoryNotes,
            pregnancyDetailsViewModel.pregnancyDetailsModel
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.pregnancyHistoryConatiner, fragment)
            .commit()
    }

    private fun scrollToTop() {
        binding.nestedScrollViewID.smoothScrollTo(0, 0)
    }

    private fun hideNextButton() {
        binding.btnLayout.btnNext.gone()
    }

    private fun showBottomNavigation() {
        binding.bottomNavigationView.visible()
    }

    override fun onDataLoaded(data: PatientListRespModel) {
        binding.loadingProgress.visible()
        viewModel.ancVisit =
            data.pregnancyDetails?.ancVisitMedicalReview?.takeIf { true }?.plus(1)?.toInt() ?: 1
        viewModel.memberId = data.memberId

        val patientDetails = supportFragmentManager.findFragmentById(R.id.pregnancyDetailsConatiner)

        if (patientDetails is MotherNeonateAncSummary || viewModel.ancVisit == 1) {
            when (patientDetails) {
                is PregnancyDetailsFragment, is MotherNeonateAncSummary -> binding.loadingProgress.gone()
                is MedicalReviewPatientDiagnosisFragment -> handleSubmit()
                else -> {
                    initializePregnancyDetailsFragment()
                    initializePregnancyHistoryFragment()
                    hideContainers()
                    setButtonWidth()
                    binding.loadingProgress.gone()
                }
            }
        } else if (viewModel.ancVisit > 1) {
            handleSubmit(false)
        }
    }

    private fun handleSummary(encounterId: String?) {
        with(binding) {
            bottomNavigationView.gone()
            referalBottomView.visible()
            btnLayout.btnNext.gone()
        }
        replaceMotherNeonateSummary(encounterId)
        scrollToTop()
        binding.loadingProgress.gone()
    }

    private fun replaceMotherNeonateSummary(encounterId: String?) {
        swipeRefresh()
        supportFragmentManager.beginTransaction()
            .replace(R.id.pregnancyDetailsConatiner, MotherNeonateAncSummary.newInstance(encounterId,patientViewModel.getPatientFHIRId()))
            .commit()
        with(supportFragmentManager) {
            findFragmentById(R.id.pregnancyHistoryConatiner)?.let {
                beginTransaction().remove(it).commit()
            }
            findFragmentById(R.id.presentingComplaintsContainer)?.let {
                beginTransaction().remove(it).commit()
            }
            findFragmentById(R.id.systemicExaminationsContainer)?.let {
                beginTransaction().remove(it).commit()
            }
            findFragmentById(R.id.clinicalNotesContainer)?.let {
                beginTransaction().remove(it).commit()
            }
        }
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        finish()
    }

    private fun replaceWithMotherNeonateAncHistoryFragment() {
        patientViewModel.getPatientId()?.let {
            val fragment = MotherNeonateAncHistoryFragment.newInstance(it,patientViewModel.getPatientFHIRId())
            supportFragmentManager.beginTransaction()
                .replace(R.id.pregnancyHistoryConatiner, fragment)
                .commit()
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backNavigation()
            }
        }
}