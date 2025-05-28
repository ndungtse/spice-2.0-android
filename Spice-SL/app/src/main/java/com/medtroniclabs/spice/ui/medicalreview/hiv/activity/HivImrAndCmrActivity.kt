package com.medtroniclabs.spice.ui.medicalreview.hiv.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.setVisible
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.data.model.HivRequestData
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.ActivityTbMedicalReviewBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.dialog.MedicalReviewSuccessDialogFragment
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.ClinicalNotesViewModel
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.diagnosis.viewmodel.DiagnosisViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.ARTRegimenFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.HIVStatusFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.HivGeneralAndSystemicExaminationFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.HivImrCmrSummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.OpportunisticInfectionsTreatmentFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.RecommendedInvestigationsDialog
import com.medtroniclabs.spice.ui.medicalreview.hiv.fragment.ViralLoadFragment
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivGeneralAndSystemicExaminationViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivImrAndCmrViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivImrCmrSummaryViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.HivViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.WhoClinicalStageViewModel
import com.medtroniclabs.spice.ui.medicalreview.hiv.viewmodel.OpportunisticInfectionViewModel
import com.medtroniclabs.spice.ui.medicalreview.investigation.InvestigationActivity
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.AncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.prescription.PrescriptionActivity
import com.medtroniclabs.spice.ui.medicalreview.tb.fragment.ComorbiditiesFragment
import com.medtroniclabs.spice.ui.medicalreview.tb.viewmodel.ComorbiditiesViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams.isViralLoadTestRecommended
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.ReferPatientFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.MotherNeonateBpWeightViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import com.medtroniclabs.spice.ui.mypatients.viewmodel.ReferPatientViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HivImrAndCmrActivity : BaseActivity(), View.OnClickListener, AncVisitCallBack,
    OnDialogDismissListener {
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val viewModel: HivImrAndCmrViewModel by viewModels()
    private lateinit var binding: ActivityTbMedicalReviewBinding
    private val hivViewModel: HivViewModel by viewModels()
    private val whoViewModel: WhoClinicalStageViewModel by viewModels()
    private val diagnosisViewModel: DiagnosisViewModel by viewModels()
    private val weightViewModel: MotherNeonateBpWeightViewModel by viewModels()
    private val presentingComplaintsViewModel: PresentingComplaintsViewModel by viewModels()
    private val hivGeneralAndSystemicExaminationViewModel: HivGeneralAndSystemicExaminationViewModel by viewModels()
    private val comorbiditiesViewModel: ComorbiditiesViewModel by viewModels()
    private val opportunisticInfectionViewModel: OpportunisticInfectionViewModel by viewModels()
    private val summaryViewModel: HivImrCmrSummaryViewModel by viewModels()
    private val referPatientViewModel: ReferPatientViewModel by viewModels()
    private val clinicalNotesViewModel: ClinicalNotesViewModel by viewModels()
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
                showErrorDialog()
            },
            callbackHome = {
                showErrorDialog(true)
            }
        )
        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
        viewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
        initStaticDataCall()
        setButtonClickListener()
        attachObserver()
        binding.btnSubmit.isEnabled = false
    }

    override fun onResume() {
        super.onResume()
        getCurrentLocation()
        swipeRefresh()
    }

    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(this)
        locationManager.getCurrentLocation {
            viewModel.lastLocation = it
        }
    }

    private fun initStaticDataCall() {
        whoViewModel.setWhoStage(MedicalReviewTypeEnums.whoClinicalStage.name)
        if (!(SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_HIV_DATA_LOADED.name))) {
            if (connectivityManager.isNetworkAvailable()) {
                hivViewModel.getHivMetaData()
            } else {
                showErrorDialogue(
                    getString(R.string.error), getString(R.string.no_internet_error),
                    isNegativeButtonNeed = false,
                ) {}
            }
        } else {
            initView()
        }
    }

    private fun attachObserver() {
        viewModel.checkRecommendationRInvestigations.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let {
                        if (it[isViralLoadTestRecommended] == true) {
                            showDialogIfNotPresent(
                                RecommendedInvestigationsDialog.TAG
                            ) {
                                RecommendedInvestigationsDialog.newInstance().apply {
                                    onOkayClickListener = {
                                        openInvestigationActivity()
                                    }
                                    onCancelClickListener = {
                                        submitImrCmrRequest()
                                    }
                                }
                            }
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {

                    }
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
                    showDialogIfNotPresent(
                        MedicalReviewSuccessDialogFragment.TAG
                    ) {
                        MedicalReviewSuccessDialogFragment.newInstance()
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
        viewModel.summaryCreateResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    showErrorDialogue(
                        title = getString(R.string.alert),
                        message = getString(R.string.something_went_wrong_try_later),
                        positiveButtonName = getString(R.string.ok),
                    ) {

                    }
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    showDialogIfNotPresent(
                        MedicalReviewSuccessDialogFragment.TAG
                    ) {
                        MedicalReviewSuccessDialogFragment.newInstance()
                    }
                }
            }
        }
        hivViewModel.hivMetaResponseLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
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

                ResourceState.SUCCESS -> {
                    hideLoading()
                    initView()
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

        viewModel.hivCreateResponse.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    resource.data?.let {
                        showSummary {
                            swipeRefresh()
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }
    private fun scrollUp() {
        binding.nestedScrollViewID.post {
            binding.nestedScrollViewID.fullScroll(View.FOCUS_UP)
        }
    }

    private fun showSummary(callBack: () -> Unit) {
        scrollUp()
        with(binding) {
            patientSummaryContainer.gone()
            comorbiditiesContainer.gone()
            presentingComplaintsContainer.gone()
            systemicExaminationsContainer.gone()
            clinicalNotesContainer.gone()
            patientHistoryContainer.gone()
            hivClinicalNotesContainer.gone()
            patientMedicalReviewDiagnosis.gone()
            hivClinicalNotesContainer.visible()
            binding.bottomNavigationView.gone()
            binding.referralBottomView.visible()
            binding.btnDone.isEnabled = true
            patientViewModel.isSummary = true
            replaceFragment(
                R.id.hivClinicalNotesContainer,
                HivImrCmrSummaryFragment.TAG,
                HivImrCmrSummaryFragment.newInstance(
                    encounterId = viewModel.hivCreateResponse.value?.data?.encounterId,
                    fhirId = patientViewModel.getPatientFHIRId()
                )
            )
        }
        callBack.invoke()
    }

    private fun setButtonClickListener() {
        binding.btnSubmit.safeClickListener(this)
        binding.btnDone.safeClickListener(this)
        binding.btnRefer.safeClickListener(this)
        binding.ivPrescription.safeClickListener(this)
        binding.ivInvestigation.safeClickListener(this)
        binding.btnRefer.safeClickListener(this)
    }

    private fun initView() {
        initializePatientDetailFragment()
    }

    private fun enableSubmitButton() {
        val isValidHiv =
            (supportFragmentManager.findFragmentByTag(HIVStatusFragment.TAG) as? HIVStatusFragment)
                ?.validateInput() == true

        val isValidFragment =
            (supportFragmentManager.findFragmentByTag(ComorbiditiesFragment.TAG) as? ComorbiditiesFragment)
                ?.validateInput(true)?.first == true

        val hasSelectedComplaints =
            presentingComplaintsViewModel.selectedPresentingComplaints.isNotEmpty()
        val hasComplaintNotes = presentingComplaintsViewModel.enteredComplaintNotes.isNotBlank()

        val hasComorbidities = comorbiditiesViewModel.chips.isNotEmpty()

        val isValidExamination =
            (supportFragmentManager.findFragmentByTag(HivGeneralAndSystemicExaminationFragment.TAG) as? HivGeneralAndSystemicExaminationFragment)
                ?.validateInput() == true

        val isValidTreatment =
            (supportFragmentManager.findFragmentByTag(OpportunisticInfectionsTreatmentFragment.TAG) as? OpportunisticInfectionsTreatmentFragment)
                ?.validateInput() == true

        binding.btnSubmit.isEnabled =
            isValidHiv || hasComplaintNotes || hasSelectedComplaints || isValidFragment || isValidExamination || isValidTreatment
        val hasClinicalNotes = clinicalNotesViewModel.enteredClinicalNotes.isNotBlank()
        if (hasComorbidities) {
            binding.btnSubmit.isEnabled = isValidFragment
            if (!isValidFragment) {
                return
            }
        }

        binding.btnSubmit.isEnabled = hasClinicalNotes
    }

    private fun initializePatientDetailFragment() {
        replaceFragment(
            R.id.patientDetailFragment,
            PatientInfoFragment.TAG,
            PatientInfoFragment.newInstance(
                intent.getStringExtra(DefinedParams.PatientId),
                isHivImrCmr = true
            ).apply {
                setDataCallback(this@HivImrAndCmrActivity)
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
                showErrorDialog()
            }
        }

    private fun showErrorDialog(isHome: Boolean = false) {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                if (isHome) {
                    startActivityWithoutSplashScreen()
                } else {
                    onBackPressPopStack()
                }
            }
        }
    }

    private fun onBackPressPopStack() {
        this@HivImrAndCmrActivity.finish()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.ivPrescription.id -> withNetworkAvailability(online = {
                openPrescriptionActivity()
            })

            binding.ivInvestigation.id -> withNetworkAvailability(online = {
                openInvestigationActivity()
            })

            binding.loadingProgress.id -> {}
            binding.btnSubmit.id -> withLocationCheck(::clickSubmit)
            binding.btnDone.id ->withLocationCheck(::createSummary)
            binding.btnRefer.id -> showReferPatientDialog()
        }
    }

    private fun callViralLoadTestRecommendation() {
        viewModel.checkRecommendationRInvestigations(patientReference = patientViewModel.getPatientId())
    }

    private fun showReferPatientDialog() {
        withNetworkAvailability(online = {
            viewModel.hivCreateResponse.value?.data?.let {
                showDialogIfNotPresent(
                    ReferPatientFragment.TAG
                ) {
                    ReferPatientFragment.newInstance(
                        MedicalReviewTypeEnums.HIV.name,
                        it.patientReference,
                        it.encounterId
                    )
                }
            }
        })
    }
    private fun createSummary() {
        val fragment = supportFragmentManager.findFragmentById(R.id.hivClinicalNotesContainer)
        if (fragment is HivImrCmrSummaryFragment) {
            val isValid = (fragment as? HivImrCmrSummaryFragment)?.validateInput()
            if (isValid == true) {
                val submitCreateId = viewModel.getSubmitCreateId()
                val nextVisitDate = DateUtils.convertDateTimeToDate(
                    summaryViewModel.nextFollowupDate,
                    DateUtils.DATE_ddMMyyyy,
                    DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ,
                    inUTC = true
                )
                if (connectivityManager.isNetworkAvailable()) {
                    viewModel.createHivSummary(
                        MedicalReviewTypeEnums.HIV.name,
                        patientViewModel.getPatientMemberId(),
                        submitCreateId,
                        patientViewModel.getPatientHouseholdId(),
                        viewModel.getPatientReference(),
                        nextVisitDate,
                        summaryViewModel.patientStatus,
                        patientViewModel.getVillageId(),
                        patientViewModel.getPatientId(),
                        MedicalReviewTypeEnums.HIV_MEDICAL_REVIEW.name,
                        summaryViewModel.eMTCTStatus,
                        summaryViewModel.maternalOutcome
                    )
                } else {
                    showErrorDialogue(
                        getString(R.string.error), getString(R.string.no_internet_error),
                        isNegativeButtonNeed = false,
                    ) {}
                }
            }
        }
    }

    private fun clickSubmit() {
        if (patientViewModel.getHivMedicalReviewStatus()) {
            callViralLoadTestRecommendation()
        } else {
            submitImrCmrRequest()
        }
    }

    private fun submitImrCmrRequest() {
        val request = createMedicalReviewRequest()
        withNetworkAvailability(online = {
            viewModel.hivCreate(request = request)
        })
    }

    private fun createMedicalReviewRequest(): HivRequestData {
        val opportunisticInfectionsData = hashMapOf<String, HashMap<String, String>>()

        opportunisticInfectionViewModel.resultHashMap.forEach { (key, valueMap) ->
            val startDate = valueMap[MedicalReviewDefinedParams.startDate]?.takeIf { it.isNotBlank() }
            val endDate = valueMap[MedicalReviewDefinedParams.endDate]?.takeIf { it.isNotBlank() }

            if (startDate != null) {
                val formattedMap = hashMapOf<String, String>().apply {
                    put(
                        MedicalReviewDefinedParams.startDate,
                        DateUtils.convertDateTimeToDate(
                            startDate,
                            DateUtils.DATE_ddMMyyyy,
                            DateUtils.DATE_FORMAT_yyyyMMdd
                        )
                    )
                    endDate?.let {
                        put(
                            MedicalReviewDefinedParams.endDate,
                            DateUtils.convertDateTimeToDate(
                                it,
                                DateUtils.DATE_ddMMyyyy,
                                DateUtils.DATE_FORMAT_yyyyMMdd
                            )
                        )
                    }
                }

                opportunisticInfectionsData[key] = formattedMap
            }
        }
        // TODO:
        return HivRequestData(
            clinicalStage = null,
            cd4 = null,
            artCode = patientViewModel.artCode,
            weight = weightViewModel.getWeight(),
            hivStatus = (supportFragmentManager.findFragmentByTag(HIVStatusFragment.TAG) as? HIVStatusFragment)?.getRequest(),
            presentingComplaints = presentingComplaintsViewModel.selectedPresentingComplaints.map { it.value }.takeIf { it.isNotEmpty() },
            presentingComplaintsNotes = presentingComplaintsViewModel.enteredComplaintNotes.takeIf { it.isNotBlank() },
            comorbiditiesCoinfectionsNotes = comorbiditiesViewModel.comments.ifBlank { null },
            systemicExaminations = hivGeneralAndSystemicExaminationViewModel.resultHashMap,
            comorbiditiesCoinfections = comorbiditiesViewModel.chips.map { it.value }.takeIf { it.isNotEmpty() },
            opportunisticInfections = opportunisticInfectionsData,
            encounter = createMedicalReviewEncounter(
                encounterId = patientViewModel.encounterId,
                patientHouseholdId = patientViewModel.getPatientHouseholdId(),
                memberId = patientViewModel.getPatientMemberId()
            ),
            clinicalNotes = clinicalNotesViewModel.enteredClinicalNotes,
            medicalReviewType = MedicalReviewTypeEnums.HIV_MEDICAL_REVIEW.name,
            id = patientViewModel.encounterId
        )
    }

    private fun createMedicalReviewEncounter(
        encounterId: String?,
        patientHouseholdId: String?,
        memberId: String?
    ): MedicalReviewEncounter {
        val currentTime = DateUtils.getCurrentDateAndTime(DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ)
        return MedicalReviewEncounter(
            id = encounterId,
            patientId = viewModel.patientId,
            provenance = ProvanceDto(),
            memberId = memberId,
            latitude = viewModel.lastLocation?.latitude ?: 0.0,
            longitude = viewModel.lastLocation?.longitude ?: 0.0,
            startTime = currentTime,
            endTime = currentTime,
            householdId = patientHouseholdId,
            referred = true,
            type = if (patientViewModel.getHivMedicalReviewStatus()) {
                MedicalReviewTypeEnums.hivCmr.name
            } else {
                MedicalReviewTypeEnums.hivImr.name
            }
        )
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
        (supportFragmentManager.findFragmentById(R.id.hivClinicalNotesContainer) as? HivImrCmrSummaryFragment)
            ?.let {
                showSummary {
                    // Do nothing
                }
            } ?: replaceWithDiagnosisFragment()
    }

    private fun replaceWithDiagnosisFragment() {
        replaceFragment(
            R.id.patientSummaryContainer,
            MedicalReviewPatientDiagnosisFragment.TAG,
            MedicalReviewPatientDiagnosisFragment.newInstance(
                isAnc = false,
                isPnc = false,
                isTB = false,
                patientId = intent.getStringExtra(DefinedParams.PatientId),
                memberID = viewModel.memberId,
                id = intent.getStringExtra(DefinedParams.ID),
                isHivImrCmr = true
            )
        )
        addOrReuseFragment(
            R.id.patientMedicalReviewDiagnosis,
            HIVStatusFragment.TAG,
            HIVStatusFragment.newInstance()
        )
        val bundle = Bundle().apply {
            putString(
                MedicalReviewTypeEnums.PresentingComplaints.name,
                MedicalReviewTypeEnums.HIV.name
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                MedicalReviewTypeEnums.HIV.name
            )
            putString(
                MedicalReviewTypeEnums.ClinicalNotes.name,
                MedicalReviewTypeEnums.HIV.name
            )
        }
        replaceFragmentInId<PresentingComplaintsFragment>(
            binding.patientHistoryContainer.id,
            bundle = bundle,
            tag = PresentingComplaintsFragment::class.simpleName
        )
        addOrReuseFragment(
            R.id.presentingComplaintsContainer,
            ComorbiditiesFragment.TAG,
            ComorbiditiesFragment.newInstance(MedicalReviewTypeEnums.HIV.name)
        )
        binding.systemicExaminationsContainer.visible()
        binding.comorbiditiesContainer.setVisible(patientViewModel.getHivMedicalReviewStatus())
        binding.clinicalNotesContainer.visible()
        binding.hivClinicalNotesContainer.visible()
        replaceFragmentOrCreateNewFragment<ARTRegimenFragment>(
            R.id.comorbiditiesContainer,
            bundle = Bundle().apply {
                putString(DefinedParams.PatientId, intent.getStringExtra(DefinedParams.PatientId))
                putString(DefinedParams.ID, intent.getStringExtra(DefinedParams.ID))
                putBoolean(DefinedParams.HIV_IMR_CMR,true)
            },
            tag = ARTRegimenFragment.TAG
        )
        addOrReuseFragment(
            R.id.systemicExaminationsContainer,
            OpportunisticInfectionsTreatmentFragment.TAG,
            OpportunisticInfectionsTreatmentFragment.newInstance()
        )
        replaceFragmentOrCreateNewFragment<HivGeneralAndSystemicExaminationFragment>(
            binding.clinicalNotesContainer.id,
            bundle = bundle,
            tag = HivGeneralAndSystemicExaminationFragment.TAG
        )
        replaceFragmentOrCreateNewFragment<ClinicalNotesFragment>(
            binding.hivClinicalNotesContainer.id,
            bundle = bundle,
            tag = ClinicalNotesFragment.TAG
        )
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.PC_ITEM, this) { _, _ ->
                enableSubmitButton()
            }
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.CF_ITEM, this) { _, _ ->
                enableSubmitButton()
            }
        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.SE_ITEM, this) { _, _ ->
                enableSubmitButton()
            }

        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.HIV_STATUS, this) { _, _ ->
                enableSubmitButton()
            }

        supportFragmentManager
            .setFragmentResultListener(MedicalReviewDefinedParams.CLINICAL_NOTES, this) { _, _ ->
                enableSubmitButton()
            }
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        startActivityWithoutSplashScreen()
    }

    fun enableRefer(isEnable: Boolean) {
        binding.btnRefer.isEnabled = isEnable
    }
}