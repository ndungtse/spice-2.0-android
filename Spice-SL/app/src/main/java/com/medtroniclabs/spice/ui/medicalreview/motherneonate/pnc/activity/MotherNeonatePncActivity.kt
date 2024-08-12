package com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.convertToUtcDateTime
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.data.model.MedicalReviewEncounter
import com.medtroniclabs.spice.data.offlinesync.model.ProvanceDto
import com.medtroniclabs.spice.databinding.ActivityMedicalReviewPncBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.formgeneration.model.FormLayout
import com.medtroniclabs.spice.formgeneration.ui.SingleSelectionCustomView
import com.medtroniclabs.spice.mappingkey.HouseHoldRegistration
import com.medtroniclabs.spice.model.PatientListRespModel
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.ClinicalNotesFragment
import com.medtroniclabs.spice.ui.medicalreview.PresentingComplaintsFragment
import com.medtroniclabs.spice.ui.medicalreview.SystemicExaminationsFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.ClinicalNotesViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.anc.fragment.PregnancyPastObstetricHistoryFragment
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.PresentingComplaintsViewModel
import com.medtroniclabs.spice.ui.medicalreview.abovefiveyears.SystemicExaminationViewModel
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.fragment.MotherNeonarePncSummaryFragment
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.listener.PncVisitCallBack
import com.medtroniclabs.spice.ui.medicalreview.motherneonate.pnc.viewmodel.MotherNeonatePNCViewModel
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewTypeEnums
import com.medtroniclabs.spice.ui.mypatients.fragment.MedicalReviewPatientDiagnosisFragment
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientInfoFragment
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MotherNeonatePncActivity : BaseActivity(), View.OnClickListener, PncVisitCallBack {
    private lateinit var binding: ActivityMedicalReviewPncBinding
    private val viewModel: MotherNeonatePNCViewModel by viewModels()
    private val presentingComplaintsViewModel: PresentingComplaintsViewModel by viewModels()
    private val systemicExaminationViewModel: SystemicExaminationViewModel by viewModels()
    private val patientViewModel: PatientDetailViewModel by viewModels()
    private val clinicalNotesViewModel: ClinicalNotesViewModel by viewModels()
    private lateinit var presentingComplaintsFragment: PresentingComplaintsFragment
    private lateinit var systemicExaminationsFragment: SystemicExaminationsFragment
    private lateinit var clinicalNotesFragment: ClinicalNotesFragment
    private lateinit var physicalExaminationFragment: PhysicalExaminationFragment
    private var TAG = "MotherNeonatePncActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicalReviewPncBinding.inflate(layoutInflater)
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
        initStaticDataCall()
        viewModel.isNeonate = false
        viewModel.patientId = intent.getStringExtra(DefinedParams.PatientId)
        buttonClickListener()
        binding.refreshLayout.setOnRefreshListener {
            swipeRefresh()
        }
    }

    private fun swipeRefresh() {
        binding.refreshLayout.isRefreshing = false
        if (binding.tvAliveStatus.text == getString(R.string.is_the_mother_alive)) {
            viewModel.isNeonate = false
            viewModel.isSwipe = true
            showLoading()
            initializePatientInfoFragment()

        } else {
            viewModel.isNeonate = true
            viewModel.isSwipe = true
            initializePatientInfoFragment()
        }

    }

    private fun initStaticDataCall() {
        if (!SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_MOTHER_LOADED_PNC.name) &&
            !SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.IS_NEONATE_LOADED_PNC.name)
        ) {
            viewModel.getMotherPncStaticData()
        } else {
            initializePatientInfoFragment()
        }
    }

    private fun buttonClickListener() {
        binding.btnSubmit.text = getString(R.string.next)
        binding.blurView.safeClickListener {}
    }

    private fun getCurrentLocation() {
        SpiceLocationManager(this).getCurrentLocation {
            viewModel.lastLocation = it
        }
    }

    private fun addReplaceFragment(containerId: Int, fragment: Fragment) {
        val existingFragment = supportFragmentManager.findFragmentById(containerId)
        supportFragmentManager.commit {
            if (existingFragment == null) {
                add(containerId, fragment)
            } else {
                replace(containerId, fragment)
            }
        }
    }

    private fun initializePatientInfoFragment() {
        val patientInfoFragment = PatientInfoFragment.newInstance(
            intent.getStringExtra(DefinedParams.PatientId),
            isPnc = true
        ).apply {
            setDataCallbackPNC(this@MotherNeonatePncActivity)
        }
        addReplaceFragment(R.id.patientDetailFragment, patientInfoFragment)
    }

    private fun initializeFragment() {
        val bundle = initializeBundle()
        setDiagnosisFragment()
        initializeAliveStatusLayout()
        setPresentingComplaintsFragment(bundle)
        if (viewModel.isNeonate) {
            setPhysicalExaminationFragment(bundle)
        } else {
            setSystemicExaminationFragment(
                bundle
            )
            setViewModelDataForMother()
        }
        setClinicalNotesFragment(bundle)
        setVisiblePncFragment()
    }

    private fun initializeBundle(): Bundle {
        val type = if (!viewModel.isNeonate) MedicalReviewTypeEnums.PNC.name.plus("-")
            .plus(MedicalReviewTypeEnums.Mother.name) else MedicalReviewTypeEnums.PNC.name.plus(
            "-"
        ).plus(MedicalReviewTypeEnums.Baby.name)
        return Bundle().apply {
            putString(
                MedicalReviewTypeEnums.PresentingComplaints.name,
                type
            )
            putString(
                MedicalReviewTypeEnums.SystemicExaminations.name,
                type
            )
        }
    }

    private fun setViewModelDataForMother() {
        if (!viewModel.isNeonate) {
            presentingComplaintsViewModel.apply {
                if (binding.tvAliveStatus.text == getString(R.string.is_the_mother_alive)) {
                    isMotherPnc = true
                    selectedPresentingComplaints = viewModel.presentingComplaints
                    enteredComplaintNotes =
                        viewModel.motherNeonatePncRequest.pncMother?.presentingComplaintsNotes ?: ""
                } else {
                    isMotherPnc = false
                }
            }
            systemicExaminationViewModel.apply {
                if (binding.tvAliveStatus.text == getString(R.string.is_the_mother_alive)) {
                    isMotherPnc = true
                    selectedSystemicExaminations = viewModel.systemicExamination
                    enteredExaminationNotes =
                        viewModel.motherNeonatePncRequest.pncMother?.systemicExaminationsNotes ?: ""
                } else isMotherPnc = false
            }
            clinicalNotesViewModel.apply {
                if (binding.tvAliveStatus.text == getString(R.string.is_the_mother_alive)) {
                    isMotherPnc = true
                    enteredClinicalNotes =
                        viewModel.motherNeonatePncRequest.pncMother?.clinicalNotes ?: ""
                } else isMotherPnc = false
            }
            viewModel.isNeonate = true
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

    private fun setDiagnosisFragment() {
        val medicalReviewPatientDiagnosisFragment =
            MedicalReviewPatientDiagnosisFragment.newInstance(
                true,
                intent.getStringExtra(DefinedParams.PatientId),
                viewModel.memberId,
                intent.getStringExtra(DefinedParams.ID)
            )
        addReplaceFragment(R.id.diagnosisFragment, medicalReviewPatientDiagnosisFragment)
    }

    private fun setPresentingComplaintsFragment(bundle: Bundle) {
        presentingComplaintsFragment =
            PresentingComplaintsFragment::class.java.getDeclaredConstructor().newInstance()
        if (viewModel.isNeonate) {
            presentingComplaintsViewModel.isMotherPnc = false
        }
        presentingComplaintsFragment.arguments = bundle
        addReplaceFragment(R.id.presentingComplaintsContainer, presentingComplaintsFragment)

    }

    private fun setSystemicExaminationFragment(bundle: Bundle) {
        systemicExaminationsFragment =
            SystemicExaminationsFragment::class.java.getDeclaredConstructor().newInstance()
        systemicExaminationsFragment.arguments = bundle
        addReplaceFragment(R.id.systemicExaminationsContainer, systemicExaminationsFragment)

    }

    private fun setPhysicalExaminationFragment(bundle: Bundle) {
        physicalExaminationFragment =
            PhysicalExaminationFragment::class.java.getDeclaredConstructor().newInstance()
        physicalExaminationFragment.arguments = bundle
        addReplaceFragment(R.id.systemicExaminationsContainer, physicalExaminationFragment)

    }

    private fun setClinicalNotesFragment(bundle: Bundle) {
        clinicalNotesFragment =
            ClinicalNotesFragment::class.java.getDeclaredConstructor().newInstance()
        if (viewModel.isNeonate) {

            clinicalNotesViewModel.isMotherPnc = false
        }
        clinicalNotesFragment.arguments = bundle
        addReplaceFragment(R.id.clinicalNotesContainer, clinicalNotesFragment)
    }


    override fun onDataLoaded(data: PatientListRespModel) {
        viewModel.pncVisit = data.pregnancyDetails?.pncVisitAssessment?.takeIf { true } ?: 1
        viewModel.memberId = data.memberId
        if (viewModel.pncVisit == 1L) {
            if (viewModel.isSwipe) {
                setDiagnosisFragment()
            } else {
                initView()
            }
        }
    }

    private fun initView() {
//        showLoading()
        initializeFragment()
        showBottomNavigation()
        setButtonClickListener()
        clinicalNotesResult()
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
                flowValue?.equals(HouseHoldRegistration.yes, ignoreCase = true) ?: false
            if (viewModel.aliveStatus == true) {
                binding.blurView.gone()
            } else {
                binding.blurView.visible()
                refreshFragments()
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
            view?.singleSelectionChildViewsOption(getString(R.string.yes))
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
        val type = if (!viewModel.isNeonate) MedicalReviewTypeEnums.PNC.name.plus("-")
            .plus(MedicalReviewTypeEnums.Mother.name) else MedicalReviewTypeEnums.PNC.name.plus(
            "-"
        ).plus(MedicalReviewTypeEnums.Baby.name)
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
    }

    private fun enableSubmitBtn() {
        binding.btnSubmit.isEnabled = clinicalNotesViewModel.enteredClinicalNotes.isNotBlank()
    }

    private fun backNavigation() {
        singleSelectValueOption(TAG)
        binding.tvAliveStatus.text = applicationContext.getString(R.string.is_the_mother_alive)
        val fragmentManager = supportFragmentManager
        val systemicExaminationsFragment =
            fragmentManager.findFragmentById(R.id.systemicExaminationsContainer)
        if (viewModel.pncVisit == 1L && systemicExaminationsFragment is SystemicExaminationsFragment) {
            // Show the dialog here
            showErrorDialog()
        } else if (viewModel.pncVisit == 1L && systemicExaminationsFragment is PhysicalExaminationFragment) {
            viewModel.isNeonate = false
            refreshPresentingComplaintsFragment()
            setSystemicExaminationFragment(initializeBundle())

        } else {
            // Show the dialog here
            showErrorDialog()
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnSubmit.id -> {
                val systemicExaminationsFragment =
                    supportFragmentManager.findFragmentById(R.id.systemicExaminationsContainer)
                if (systemicExaminationsFragment is PhysicalExaminationFragment) {
                    showLoading()
                    viewModel.saveMotherNeonatePncData()
                    pncSummary()
                    neoNateSubmit()
                    scrollToTop()
                } else {
                    binding.blurView.visible()
                    resetSelectionViews(TAG)
                    neonateFlow()
                    motherSubmit()
                    scrollToTop()
                }
            }
        }
    }

    private fun neoNateSubmit() {
        viewModel.motherNeonatePncRequest.apply {
            pncChild?.apply {
                presentingComplaints =
                    presentingComplaintsViewModel.selectedPresentingComplaints.map { it.value }
                presentingComplaintsNotes =
                    presentingComplaintsViewModel.enteredComplaintNotes
                physicalExaminations =
                    systemicExaminationViewModel.selectedSystemicExaminations.map { it.value }
                physicalExaminationsNotes =
                    systemicExaminationViewModel.enteredExaminationNotes
                clinicalNotes = clinicalNotesViewModel.enteredClinicalNotes
                isChildAlive = viewModel.aliveStatus

                breastFeeding = true
                exclusiveBreastFeeding = true
                cordExamination = "Abnormel"
                congenitalDetect = "Notes involutionsOfTheUterusNotes"

                encounter = MedicalReviewEncounter(
                    patientId = viewModel.patientId,
                    provenance = ProvanceDto(
                        createdDateTime = System.currentTimeMillis().convertToUtcDateTime()
                    ),
                    latitude = viewModel.lastLocation?.latitude ?: 0.0,
                    longitude = viewModel.lastLocation?.longitude ?: 0.0,
                    startTime = DateUtils.getCurrentDateAndTime(
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    ),
                    endTime = DateUtils.getCurrentDateAndTime(
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    ),
                    referred = true,
                    visitNumber = viewModel.pncVisit.toInt()
                )
            }

        }
    }

    private fun motherSubmit() {
        viewModel.motherNeonatePncRequest.apply {
            pncMother?.apply {
                presentingComplaints =
                    presentingComplaintsViewModel.selectedPresentingComplaints.map { it.value }
                presentingComplaintsNotes =
                    presentingComplaintsViewModel.enteredComplaintNotes
                systemicExaminations =
                    systemicExaminationViewModel.selectedSystemicExaminations.map { it.value }
                systemicExaminationsNotes =
                    systemicExaminationViewModel.enteredExaminationNotes
                clinicalNotes = clinicalNotesViewModel.enteredClinicalNotes
                isMotherAlive = viewModel.aliveStatus
                breastCondition = "Normal"
                involutionsOfTheUterus = "Normal"
                encounter = MedicalReviewEncounter(
                    patientId = viewModel.patientId,
                    provenance = ProvanceDto(
                        createdDateTime = System.currentTimeMillis().convertToUtcDateTime()
                    ),
                    latitude = viewModel.lastLocation?.latitude ?: 0.0,
                    longitude = viewModel.lastLocation?.longitude ?: 0.0,
                    startTime = DateUtils.getCurrentDateAndTime(
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    ),
                    endTime = DateUtils.getCurrentDateAndTime(
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    ),
                    referred = true,
                    visitNumber = viewModel.pncVisit.toInt()
                )
            }

        }
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

    private fun pncSummary() {
        binding.bottomNavigationView.gone()
        binding.tvAliveStatus.gone()
        binding.systemicExaminationsContainer.gone()
        binding.clinicalNotesContainer.gone()
        binding.presentingComplaintsContainer.gone()
        binding.referalBottomView.visible()
        val motherNeonatePncSummaryFragment =
            MotherNeonarePncSummaryFragment.newInstance()
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

    private fun clinicalNotesResult() {
        supportFragmentManager
            .setFragmentResultListener(
                MedicalReviewDefinedParams.CLINICAL_NOTES,
                this
            ) { _, _ ->
                enableSubmitBtn()
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
        this@MotherNeonatePncActivity.finish()
    }

    private fun setButtonClickListener() {
        binding.btnSubmit.safeClickListener(this@MotherNeonatePncActivity)
    }

    private fun scrollToTop() {
        binding.nestedScrollViewID.smoothScrollTo(0, 0)
    }

    private fun attachObservers() {
        viewModel.pncSaveResponse.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()

                }

                ResourceState.ERROR -> {
                    hideLoading()
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
                }
            }
        }

        viewModel.motherMetaResponse.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
//                    hideLoading()
                    viewModel.getNeonatePncStaticData()
                }

                ResourceState.ERROR -> {
//                    hideLoading()
                }
            }
        }
        viewModel.motherMetaResponse.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
//                    hideLoading()
                    initializePatientInfoFragment()
                }

                ResourceState.ERROR -> {
//                    hideLoading()
                }
            }
        }


    }
}

