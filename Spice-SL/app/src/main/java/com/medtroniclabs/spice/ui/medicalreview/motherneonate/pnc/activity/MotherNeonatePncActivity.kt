package com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.data.model.MotherNeonateAncRequest
import com.medtroniclabs.spice.data.model.PncSubmitResponse
import com.medtroniclabs.spice.databinding.ActivityMedicalReviewPncBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.Resource
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.dialog.MedicalReviewSuccessDialogFragment
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.GeneralExaminationFragment
import com.medtroniclabs.spice.ui.medicalreview.PhysicalExaminationFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.ClinicalNotesViewModel
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.investigation.InvestigationActivity
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.fragment.MotherNeonatePncSummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.viewmodel.MotherNeonatePNCViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.viewmodel.MotherNeonatePncSummaryViewModel
import com.medtroniclabs.spice.ui.medicalreview.prescription.PrescriptionActivity
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.medicalreview.viewmodel.GeneralExaminationViewModel
import com.medtroniclabs.spice.ui.medicalreview.viewmodel.PhysicalExaminationViewModel
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.ReferPatientFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.MotherNeonateBpWeightViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.ReferPatientViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MotherNeonatePncActivity : BaseActivity(), View.OnClickListener, AncVisitCallBack,
    OnDialogDismissListener {

    private lateinit var binding: ActivityMedicalReviewPncBinding

    // ViewModels
    private val viewModel: MotherNeonatePNCViewModel by viewModels()
    private val bpWeightViewModel: MotherNeonateBpWeightViewModel by viewModels()
    private val presentingComplaintsViewModel: PresentingComplaintsViewModel by viewModels()
    private val systemicExaminationViewModel: GeneralExaminationViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val clinicalNotesViewModel: ClinicalNotesViewModel by viewModels()
    private val physicalExaminationViewModel: PhysicalExaminationViewModel by viewModels()
    private val motherNeonatePncSummaryViewModel: MotherNeonatePncSummaryViewModel by viewModels()
    private val referPatientViewModel: ReferPatientViewModel by viewModels()


    // Fragments
    private lateinit var presentingComplaintsFragment: PresentingComplaintsFragment
    private lateinit var systemicExaminationsFragment: GeneralExaminationFragment
    private lateinit var clinicalNotesFragment: ClinicalNotesFragment
    private lateinit var physicalExaminationFragment: PhysicalExaminationFragment

    companion object {
        const val TAG = "MotherNeonatePncActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        setupMainContentView()
        initialize()
    }

    private fun setupBinding() {
        binding = ActivityMedicalReviewPncBinding.inflate(layoutInflater)
    }

    private fun setupMainContentView() {
        setMainContentView(
            binding.root,
            true,
            getString(R.string.patient_medical_review),
            homeAndBackVisibility = Pair(true, true),
            callback = {
                backNavigation()
            },
            callbackHome = {
                backNavigationToHome()
            }
        )
    }

    private fun initialize() {
        attachObservers()
        getCurrentLocation()
        initStaticDataCall()
        initializeViewModel()
        initializeClickListener()
        setupRefreshLayout()
    }

    private fun initializeViewModel() {
        viewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
    }

    private fun setupRefreshLayout() = binding.refreshLayout.setOnRefreshListener { swipeRefresh() }

    private fun swipeRefresh() {
        binding.refreshLayout.isRefreshing = false
        viewModel.isNeonate = binding.tvAliveStatus.text != getString(R.string.is_the_mother_alive)
        viewModel.isSwipe = true
        showLoading()
        withNetworkCheck(connectivityManager, ::refreshPatientDetails, ::isRefreshStatus)
    }

    private fun refreshPatientDetails() {
        getFragmentById(supportFragmentManager, (R.id.patientDetailFragment))
            .let {
                patientViewModel.getPatientId()?.let { id ->
                    patientViewModel.getPatients(id)
                }
            }
    }

    private fun initStaticDataCall() {
        if (isPncDataNotLoaded()) {
            withNetworkCheck(
                connectivityManager,
                onNetworkAvailable = { ::viewModel.get().getMotherPncStaticData() },
                onNetworkNotAvailable = ::isRefreshStatus
            )
        } else {
            initializePatientInfoFragment()
        }
    }

    private fun isPncDataNotLoaded(): Boolean {
        return !SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_MOTHER_LOADED_PNC.name) &&
                !SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_NEONATE_LOADED_PNC.name)
    }

    private fun initializeClickListener() {
        binding.btnSubmit.text = getString(R.string.next)
        binding.blurView.safeClickListener {}
        binding.btnRefer.safeClickListener(this@MotherNeonatePncActivity)
        binding.btnSubmit.safeClickListener(this@MotherNeonatePncActivity)
        binding.btnDone.safeClickListener(this@MotherNeonatePncActivity)
        binding.ivPrescription.safeClickListener(this@MotherNeonatePncActivity)
        binding.ivInvestigation.safeClickListener(this@MotherNeonatePncActivity)

    }

    private fun getCurrentLocation() {
        SpiceLocationManager(this).getCurrentLocation {
            viewModel.lastLocation = it
        }
    }

    private fun addReplaceFragment(containerId: Int, fragment: Fragment) {
        val existingFragment = getFragmentById(supportFragmentManager, containerId)
        supportFragmentManager.commit {
            if (existingFragment == null) {
                add(containerId, fragment)
            } else {
                replace(containerId, fragment)
            }
        }
    }

    private fun initializeFragment() {
        val bundle = initializeBundle()
        initializeDiagnosisFragment()
        initializeAliveStatusLayout()
        setFragmentsBasedOnNeonateStatus(bundle)
        initializeClinicalNotesFragment(bundle)
        setVisiblePncFragment()
    }

    private fun setFragmentsBasedOnNeonateStatus(bundle: Bundle) {
        initializePresentingComplaintsFragment(bundle)
        if (viewModel.isNeonate) {
            initializePhysicalExaminationFragment(bundle)
        } else {
            initializeSystemicExaminationFragment(bundle)
            setViewModelDataForMother()
        }
    }

    private fun initializeBundle(): Bundle {
        val type = if (viewModel.isNeonate) {
            MedicalReviewTypeEnums.PNC_CHILD_REVIEW.name
        } else {
            MedicalReviewTypeEnums.PNC_MOTHER_REVIEW.name
        }

        return Bundle().apply {
            putString(MedicalReviewTypeEnums.PresentingComplaints.name, type)
            putString(
                if (viewModel.isNeonate) {
                    MedicalReviewTypeEnums.ObstetricExaminations.name
                } else {
                    MedicalReviewTypeEnums.SystemicExaminations.name
                },
                type
            )
            putString(
                MedicalReviewTypeEnums.DiagnosisType.name,
                MedicalReviewTypeEnums.PNC_MOTHER_REVIEW.name
            )
            putString(
                DefinedParams.PatientId,
                intent.getStringExtra(DefinedParams.PatientId)
            )
            putString(
                DefinedParams.MemberID,
                viewModel.memberId
            )
            putString(
                DefinedParams.ID,
                intent.getStringExtra(DefinedParams.ID)
            )
        }
    }

    private fun setViewModelDataForMother() {
        if (!viewModel.isNeonate) {
            val isMotherAlive =
                binding.tvAliveStatus.text == getString(R.string.is_the_mother_alive)
            updatePresentingComplaintsViewModel(isMotherAlive)
            updateSystemicExaminationViewModel(isMotherAlive)
            updateClinicalNotesViewModel(isMotherAlive)
        }
    }

    private fun updatePresentingComplaintsViewModel(isMotherAlive: Boolean) {
        presentingComplaintsViewModel.apply {
            isMotherPnc = isMotherAlive
            if (isMotherAlive) {
                selectedPresentingComplaints = viewModel.presentingComplaints
                enteredComplaintNotes =
                    viewModel.motherNeonatePncRequest.pncMother?.presentingComplaintsNotes ?: ""
            }
        }
    }

    private fun updateSystemicExaminationViewModel(isMotherAlive: Boolean) {
        systemicExaminationViewModel.apply {
            isMotherPnc = isMotherAlive
            if (isMotherAlive) {
                selectedSystemicExaminations = viewModel.systemicExamination
                enteredExaminationNotes =
                    viewModel.motherNeonatePncRequest.pncMother?.systemicExaminationsNotes ?: ""
            }
        }
    }

    private fun updateClinicalNotesViewModel(isMotherAlive: Boolean) {
        clinicalNotesViewModel.apply {
            isMotherPnc = isMotherAlive
            if (isMotherAlive) {
                enteredClinicalNotes =
                    viewModel.motherNeonatePncRequest.pncMother?.clinicalNotes ?: ""
            }
        }
    }

    private fun breastConditionNotes() {
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.GENERAL_SE_ITEM, this) { _, _ ->
            }
    }

    private fun setVisiblePncFragment() {
        with(binding) {
            patientDetailFragment.visible()
            diagnosisFragment.visible()
            presentingComplaintsContainer.visible()
            systemicExaminationsContainer.visible()
            clinicalNotesContainer.visible()
        }
    }

    // Initialize the fragments
    private fun initializePatientInfoFragment() {
        val patientInfoFragment = PatientInfoFragment.newInstance(
            intent.getStringExtra(DefinedParams.PatientId), isPnc = true
        ).apply { setDataCallback(this@MotherNeonatePncActivity) }
        addReplaceFragment(R.id.patientDetailFragment, patientInfoFragment)
    }

    private fun initializeDiagnosisFragment() {
        val medicalReviewPatientDiagnosisFragment =
            MedicalReviewPatientDiagnosisFragment.newInstance(
                isAnc = false,
                isPnc = true,
                patientId = intent.getStringExtra(DefinedParams.PatientId),
                memberID = viewModel.memberId,
                id = intent.getStringExtra(DefinedParams.ID)
            )
        addReplaceFragment(R.id.diagnosisFragment, medicalReviewPatientDiagnosisFragment)
    }

    private fun initializePresentingComplaintsFragment(bundle: Bundle) {
        presentingComplaintsFragment =
            PresentingComplaintsFragment::class.java.getDeclaredConstructor().newInstance()
        if (viewModel.isNeonate) {
            presentingComplaintsViewModel.isMotherPnc = false
        }
        presentingComplaintsFragment.arguments = bundle
        addReplaceFragment(R.id.presentingComplaintsContainer, presentingComplaintsFragment)

    }

    private fun initializeSystemicExaminationFragment(bundle: Bundle) {
        systemicExaminationsFragment =
            GeneralExaminationFragment()::class.java.getDeclaredConstructor().newInstance()
        systemicExaminationsFragment.arguments = bundle
        addReplaceFragment(R.id.systemicExaminationsContainer, systemicExaminationsFragment)
    }

    private fun initializePhysicalExaminationFragment(bundle: Bundle) {
        physicalExaminationFragment =
            PhysicalExaminationFragment::class.java.getDeclaredConstructor().newInstance()
        physicalExaminationFragment.arguments = bundle
        addReplaceFragment(R.id.systemicExaminationsContainer, physicalExaminationFragment)
    }

    private fun initializeClinicalNotesFragment(bundle: Bundle) {
        clinicalNotesFragment =
            ClinicalNotesFragment::class.java.getDeclaredConstructor().newInstance()
        if (viewModel.isNeonate) {
            clinicalNotesViewModel.isMotherPnc = false
        }
        clinicalNotesFragment.arguments = bundle
        addReplaceFragment(R.id.clinicalNotesContainer, clinicalNotesFragment)
    }


    override fun onDataLoaded(details: PatientListRespModel) {
            viewModel.pncVisit = details.pregnancyDetails?.pncVisitMedicalReview?.takeIf { true }?.plus(1) ?: 1
        //        viewModel.getChildMemberId(details.childPatientId)

        viewModel.memberId = details.memberId
        if (viewModel.isSwipe) {
                val summaryFragment =
                    getFragmentById(supportFragmentManager, (R.id.diagnosisFragment))
                if (summaryFragment is MotherNeonatePncSummaryFragment) {
                    viewModel.saveMotherNeonatePncData()
                } else {
                    bpWeightViewModel.fetchWeight(MotherNeonateAncRequest(memberId = viewModel.memberId))
                    bpWeightViewModel.fetchBloodPressure(MotherNeonateAncRequest(memberId = viewModel.memberId))
                }
            } else {
                hideLoading()
                initView()
            }
    }

    private fun initView() {
        initializeFragment()
        showBottomNavigation()
        summaryFragmentResult()
    }

    private fun initializeAliveStatusLayout() {
        getAliveStatusFlowData().let {
            val view = SingleSelectionCustomView(binding.root.context)
            view.tag = TAG
            view.addViewElements(
                it,
                false,
                viewModel.resultFlowHashMap,
                Pair(TAG, null),
                FormLayout(
                    viewType = "",
                    id = "",
                    title = "",
                    visibility = "",
                    optionsList = null
                ),
                singleSelectionCallback
            )
            binding.btnLayout.addView(view)
        }
    }

    private var singleSelectionCallback: ((selectedID: Any?, elementId: Pair<String, String?>, serverViewModel: FormLayout, name: String?) -> Unit)? =
        { selectedID, _, _, _ ->
            viewModel.resultFlowHashMap[TAG] =
                selectedID as String
            val flowValue =
                viewModel.resultFlowHashMap[TAG] as? String
            viewModel.aliveStatus =
                flowValue?.let { CommonUtils.getIsBooleanFromString(it) }
            validateStatus(flowValue)
        }

    private fun validateStatus(flowValue: String?) {
        viewModel.aliveStatus?.let {
            if (it) {
                    if (viewModel.motherLiveStatus == getString(R.string.no)) {
                        if(binding.tvAliveStatus.text==(getString(R.string.is_the_baby_alive))) {
                            updatePrescriptionInvestigationVisible(false)
                        }else{
                            updatePrescriptionInvestigationVisible(true)
                        }
                    } else {
                        updatePrescriptionInvestigationVisible(true)
                    }
                binding.blurView.gone()
                binding.btnSubmit.isEnabled = true
            } else {
                updatePrescriptionInvestigationVisible(false)
                clinicalNotesViewModel.enteredClinicalNotes = ""
                presentingComplaintsViewModel.selectedPresentingComplaints = arrayListOf()
                presentingComplaintsViewModel.enteredComplaintNotes = ""
                binding.btnSubmit.isEnabled = true
                binding.blurView.visible()
                refreshFragments()
            }
            motherAliveStatus(flowValue)
        }
    }

    private fun motherAliveStatus(aliveStatus: String?) {
        if (binding.tvAliveStatus.text == getString(R.string.is_the_mother_alive)) {
            viewModel.motherLiveStatus = aliveStatus
        }
    }

    private fun resetSelectionViews(vararg viewTags: String) {
        viewTags.forEach { tag ->
            val view = binding.root.findViewWithTag<SingleSelectionCustomView>(tag)
            view?.resetSingleSelectionChildViews()
        }
    }

    private fun singleSelectValueOption(vararg viewTags: String) {
        viewTags.forEach { tag ->
            val view = binding.root.findViewWithTag<SingleSelectionCustomView>(tag)
            viewModel.motherLiveStatus?.let { view?.singleSelectionChildViewsOption(it) }
        }
    }

    private fun refreshFragments() {
        presentingComplaintsFragment.refreshFragment()
        if (binding.tvAliveStatus.text == getString(R.string.is_the_baby_alive)) {
            physicalExaminationFragment.refreshFragment()
        } else {
            systemicExaminationsFragment.refreshFragment()
        }
        clinicalNotesFragment.refreshFragment()
    }

    private fun refreshPresentingComplaintsFragment() {
        val type = if (!viewModel.isNeonate) MedicalReviewTypeEnums.PNC_MOTHER_REVIEW.name else MedicalReviewTypeEnums.PNC_CHILD_REVIEW.name
        presentingComplaintsFragment.refreshPresentingFragment(type, viewModel.presentingComplaints)
        clinicalNotesFragment.reloadFragment(
            viewModel.motherNeonatePncRequest.pncMother?.clinicalNotes ?: ""
        )
    }

    private fun getAliveStatusFlowData(): ArrayList<Map<String, Any>> {
        val flowList = ArrayList<Map<String, Any>>()
        flowList.add(
            CommonUtils.getOptionMap(
                getString(R.string.yes),
                getString(R.string.yes)
            )
        )
        flowList.add(
            CommonUtils.getOptionMap(
                getString(R.string.no),
                getString(R.string.no)
            )
        )
        return flowList
    }

    private fun showBottomNavigation() {
        binding.bottomNavigationView.visible()
        updatePrescriptionInvestigationVisible(false)
    }

    private fun backNavigation() {
        onBackPress()
    }

    private fun backNavigationToHome() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                startActivityWithoutSplashScreen()
            }
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPress()
            }
        }

    private fun onBackPress() {
        singleSelectValueOption(TAG)
        binding.tvAliveStatus.text = applicationContext.getString(R.string.is_the_mother_alive)
        val fragmentManager = supportFragmentManager
        val systemicExaminationsFragment =
            fragmentManager.findFragmentById(R.id.systemicExaminationsContainer)
        val summaryFragment =
            getFragmentById(supportFragmentManager, (R.id.diagnosisFragment))
        if (summaryFragment is MotherNeonatePncSummaryFragment) {
            backNavigationToHome()
        } else if ( systemicExaminationsFragment is PhysicalExaminationFragment) {
            viewModel.isNeonate = false
            refreshPresentingComplaintsFragment()
            initializeSystemicExaminationFragment(initializeBundle())
        } else if ( systemicExaminationsFragment is GeneralExaminationFragment) {
            backMenuFlow()
        } else {
            backMenuFlow ()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnSubmit.id ->withNetworkCheck(connectivityManager, ::validateAndSubmitRequest)
            binding.btnDone.id -> handleBtnDoneClick()
            binding.ivPrescription.id -> handleButtonPrescription()
            binding.ivInvestigation.id -> handleInvestigation()
            binding.btnRefer.id -> handleButtonRefer()
        }
    }

    private fun handleInvestigation() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { data ->
            val intent = Intent(this, InvestigationActivity::class.java)
            intent.putExtra(DefinedParams.PatientId, data.patientId)
            intent.putExtra(DefinedParams.EncounterId,patientViewModel.encounterId)
            getResult.launch(intent)
        }
    }

    private fun handleButtonRefer() {
        viewModel.pncSaveResponse.value?.data?.let {
            ReferPatientFragment.newInstance(
                MedicalReviewTypeEnums.PNC_MOTHER_REVIEW.name,
                it.patientReference,
                it.encounterId
            ).show(supportFragmentManager, ReferPatientFragment.TAG)
        }
    }

    private fun handleButtonPrescription() {
        patientViewModel.patientDetailsLiveData.value?.data?.let { data ->
            val intent = Intent(this, PrescriptionActivity::class.java)
            intent.putExtra(DefinedParams.PatientId, data.patientId)
            intent.putExtra(DefinedParams.EncounterId, patientViewModel.encounterId)
            intent.putExtra(DefinedParams.IsNeonate, false)
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

    private fun handleBtnDoneClick() {
        if (areMotherAndChildNotAlive()) {
            startActivityWithoutSplashScreen()
        } else {
            patientViewModel.patientDetailsLiveData.value?.data?.let { details ->
                val motherDetails =
                    motherNeonatePncSummaryViewModel.pncSummaryResponse.value?.data?.pncMother
                val motherPatientStatus = motherNeonatePncSummaryViewModel.patientStatusMother
                val motherNextVisitDate = motherNeonatePncSummaryViewModel.nextFollowupDate
                withNetworkCheck(
                    connectivityManager,
                    {
                        ::viewModel.get().summaryCreatePncData(
                            motherDetails,
                            motherPatientStatus,
                            motherNextVisitDate,
                            details
                        )
                    },
                    ::isRefreshStatus
                )
            }
        }
    }

    private fun enableDoneBtn() {
        val patientStatus = motherNeonatePncSummaryViewModel.patientStatusMother
        binding.btnDone.isEnabled = when {
            patientStatus == ReferralStatus.OnTreatment.name && motherNeonatePncSummaryViewModel.nextFollowupDate != null -> true
            patientStatus == DefinedParams.Recovered -> true
            else -> false
        }
    }

    private fun handleBtnSubmitClick() {
        val systemicExaminationsFragment =
            getFragmentById(supportFragmentManager, (R.id.systemicExaminationsContainer))
        if (systemicExaminationsFragment is PhysicalExaminationFragment) {
            processNeonatePNC()
        } else {
            scrollToTop()
            processMotherPNC()
        }
    }

    private fun processNeonatePNC() {
        showLoading()
        scrollToTop()
        neoNateSubmit()
    }

    private fun processMotherPNC() {
        binding.blurView.visible()
        binding.btnSubmit.isEnabled = false
        updatePrescriptionInvestigationVisible(false)
        resetSelectionViews(TAG)
        neonateFlow()
        motherSubmit()
    }

    private fun neoNateSubmit() {
        viewModel.setNeonateDetailsReq(
            physicalExaminationViewModel,
            presentingComplaintsViewModel,
            patientViewModel,
            clinicalNotesViewModel
        )
        if (areMotherAndChildNotAlive()) {
            binding.btnDone.isEnabled = true
            binding.btnRefer.gone()
        }
    }

    private fun motherSubmit() {
        breastConditionNotes()
        viewModel.setMotherDetailsReq(
            systemicExaminationViewModel,
            clinicalNotesViewModel,
            presentingComplaintsViewModel,
            patientViewModel
        )
        setBackMotherData()
    }


    private fun setBackMotherData() {
        viewModel.presentingComplaints =
            presentingComplaintsViewModel.selectedPresentingComplaints
        viewModel.systemicExamination =
            systemicExaminationViewModel.selectedSystemicExaminations
        viewModel.motherNeonatePncRequest.pncMother?.presentingComplaintsNotes =
            presentingComplaintsViewModel.enteredComplaintNotes
        viewModel.motherNeonatePncRequest.pncMother?.systemicExaminationsNotes =
            systemicExaminationViewModel.enteredExaminationNotes
        viewModel.motherNeonatePncRequest.pncMother?.clinicalNotes =
            clinicalNotesViewModel.enteredClinicalNotes
    }

    private fun pncSummary(resource: Resource<PncSubmitResponse>) {
        binding.bottomNavigationView.gone()
        binding.tvAliveStatus.gone()
        binding.systemicExaminationsContainer.gone()
        binding.clinicalNotesContainer.gone()
        binding.presentingComplaintsContainer.gone()
        binding.referalBottomView.visible()
        motherNeonatePncSummaryViewModel.motherNeonatePncSummaryRequest.apply {
            id = resource.data?.encounterId
            childId = resource.data?.childEncounterId
            childPatientReference = resource.data?.childPatientReference
            patientReference = resource.data?.patientReference
        }

        if (viewModel.motherNeonatePncRequest.pncMother?.isMotherAlive == false &&
            viewModel.motherNeonatePncRequest.pncChild?.isChildAlive == false
        ) {
            motherNeonatePncSummaryViewModel.motherNeonateAlive = false
        }
        val motherNeonatePncSummaryFragment =
            MotherNeonatePncSummaryFragment.newInstance()
        addReplaceFragment(R.id.diagnosisFragment, motherNeonatePncSummaryFragment)
    }

    // Is baby alive flow
    private fun neonateFlow() {
        binding.tvAliveStatus.text = applicationContext.getString(R.string.is_the_baby_alive)
        if (binding.btnSubmit.text == applicationContext.getString(R.string.next)) {
            viewModel.isNeonate = true
            initializeFragment()
        }
    }

    private fun summaryFragmentResult() {
        supportFragmentManager
            .setFragmentResultListener(
                MedicalReviewDefinedParams.SUMMARY_ITEM,
                this
            ) { _, _ ->
                enableDoneBtn()
            }
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.NOT_ALIVE, this) { _, _ ->
                summarySuccessDialog()
            }
    }
    private fun backMenuFlow() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive){
                onBackPressPopStack()
            }
        }
    }
    private fun onReferPatient(){
        val fragment =
            supportFragmentManager.findFragmentByTag(ReferPatientFragment.TAG) as? ReferPatientFragment
        fragment?.dismiss()
        MedicalReviewSuccessDialogFragment.newInstance().show(
            supportFragmentManager,
            MedicalReviewSuccessDialogFragment.TAG
        )
    }

    private fun onBackPressPopStack() {
        this@MotherNeonatePncActivity.finish()
    }

    private fun scrollToTop() {
        binding.nestedScrollViewID.smoothScrollTo(0, 0)
    }

    private fun attachObservers() {
        referPatientViewModel.referPatientResultLiveData.observe(this) { resource ->
            handleResourceState(
                resource, onSuccess = { onReferPatient() },
                onBackPressPopStack = ::onBackPressPopStack
            )
        }
        viewModel.pncSaveResponse.observe(this) { resource ->
            handleResourceState(
                resource, onSuccess = { pncSummary(resource) },
                onBackPressPopStack = ::onBackPressPopStack
            )
        }
        patientViewModel.patientDetailsLiveData.observe(this) { resource ->
            handleResourceState(resource, onSuccess = {
                if (binding.refreshLayout.isRefreshing) {
                    binding.refreshLayout.isRefreshing = false
                }
            }, onBackPressPopStack = ::onBackPressPopStack)
        }
        viewModel.childDetailsLiveData.observe(this) { resource ->
            handleResourceState(resource, onSuccess = {
                viewModel.childMemberId = resource.data?.memberId
            }, onBackPressPopStack = ::onBackPressPopStack)
        }
        viewModel.motherMetaResponse.observe(this) { resource ->
            handleResourceState(
                resource, onSuccess = { viewModel.getNeonatePncStaticData() },
                onBackPressPopStack = ::onBackPressPopStack
            )
        }

        viewModel.neonateMetaResponse.observe(this) { resource ->
            handleResourceState(
                resource, onSuccess = { initializePatientInfoFragment() },
                onBackPressPopStack = ::onBackPressPopStack
            )
        }
        viewModel.summaryCreateResponse.observe(this) { resource ->
            handleResourceState(
                resource, onSuccess = { summarySuccessDialog() },
                onBackPressPopStack = ::onBackPressPopStack
            )
        }
    }

    private fun summarySuccessDialog() {
        MedicalReviewSuccessDialogFragment.newInstance().show(
            supportFragmentManager,
            MedicalReviewSuccessDialogFragment.TAG
        )
    }

    private fun validateAndSubmitRequest() {
        if( viewModel.aliveStatus != true) {
            handleBtnSubmitClick()
        }else {
            val clFragment = getFragmentById(
                supportFragmentManager,
                (R.id.clinicalNotesContainer)
            ) as? ClinicalNotesFragment
            clFragment?.let {
                if ((it.validateInput() && viewModel.aliveStatus == true)) {
                    handleBtnSubmitClick()
                }
            }
        }
    }

    private fun isRefreshStatus() =
        binding.refreshLayout.takeIf { it.isRefreshing }?.let { it.isRefreshing = false }

    private fun View.setVisible(isVisible: Boolean) {
        this.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    private fun updatePrescriptionInvestigationVisible(isVisible: Boolean) {
        binding.ivInvestigation.setVisible(isVisible)
        binding.ivPrescription.setVisible(isVisible)
        binding.ivInvestigationMarginOne.setVisible(isVisible)
        binding.ivInvestigationMarginTwo.setVisible(isVisible)
        binding.viewLine.setVisible(isVisible)
        binding.ivPrescriptionMarginOne.setVisible(isVisible)
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        startActivityWithoutSplashScreen()
    }

    private fun areMotherAndChildNotAlive(): Boolean {
        return viewModel.motherNeonatePncRequest.pncMother?.isMotherAlive == false &&
                viewModel.motherNeonatePncRequest.pncChild?.isChildAlive == false
    }
}

