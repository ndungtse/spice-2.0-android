package com.medtroniclabs.spice.ui.assessment

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.startBackgroundOfflineSync
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.databinding.ActivityAssessmentBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.mappingkey.Screening
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.OtherSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Summary
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentFamilyPlanningFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentFamilyPlanningSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentICCMFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentICCMSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentNCDFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentNCDSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentOtherSymptomSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentOtherSymptomsFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentPregnantWomenRegistrationFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentPregnantWomenRegistrationSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentRMNCHFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentRMNCHNeonateFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentRMNCHNeonateSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentRMNCHSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentSLNCDFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentSLNCDSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentTBFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentTBSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.RxBuddySummaryFragment
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ANC_MENU
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.ChildHoodVisit
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.DeathOfMother
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PNC
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.PNCNeonatal
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.deathOfBaby
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentRMNCHNeonateViewModel
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.medtroniclabs.spice.ui.cbs.activity.CbsActivity
import com.medtroniclabs.spice.ui.followup.FollowUpMyPatientActivity
import com.medtroniclabs.spice.ui.household.HouseholdSearchActivity
import com.medtroniclabs.spice.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class AssessmentActivity : BaseActivity() {
    private lateinit var binding: ActivityAssessmentBinding
    private val viewModel: AssessmentViewModel by viewModels()
    private val assessmentRMNCHNeonateViewModel: AssessmentRMNCHNeonateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityAssessmentBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.assessment),
            homeAndBackVisibility = Pair(true, true),
            callback = {
                backNavigation(false)
            },
            callbackHome = {
                viewModel.setUserJourney(AnalyticsDefinedParams.ONHOMEBUTTONTRIGGERED)
                backNavigation(true)
            },
        )
        getIntentValue()
        loadFragment()
        attachObservers()
    }

    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(this)
        locationManager.getCurrentLocation {
            viewModel.setCurrentLocation(it)
        }
    }

    private fun backNavigation(isHome: Boolean) {
        val backButtonStatus = getBackButtonStatus()
        if (backButtonStatus.first) {
            showErrorDialogue(
                getString(R.string.alert),
                getString(R.string.exit_reason),
                isNegativeButtonNeed = true,
            ) { isPositive ->
                if (isPositive) {
                    viewModel.isAssessmentCancelLiveData.value = true
                    navigationHandling(isHome, backButtonStatus.second)
                }
            }
        } else {
            navigationHandling(isHome, backButtonStatus.second)
        }
    }

    /**
     * First boolean - Changes in page
     * Second boolean - Summary page or not
     */
    private fun getBackButtonStatus(): Pair<Boolean, Boolean> {
        when (val fragment = supportFragmentManager.findFragmentById(R.id.formsFragmentContainer)) {
            is AssessmentRMNCHFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), false)
            }

            is AssessmentICCMFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), false)
            }

            is AssessmentOtherSymptomsFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), false)
            }

            is AssessmentRMNCHNeonateFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), false)
            }

            is AssessmentICCMSummaryFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), true)
            }

            is AssessmentOtherSymptomSummaryFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), true)
            }

            is AssessmentRMNCHSummaryFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), true)
            }

            is AssessmentRMNCHNeonateSummaryFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), true)
            }

            is AssessmentNCDFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), true)
            }

            is AssessmentTBFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), false)
            }

            is AssessmentTBSummaryFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), true)
            }

            is AssessmentNCDSummaryFragment -> {
                return Pair(false, false)
            }

            is AssessmentSLNCDFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), false)
            }

            is AssessmentSLNCDSummaryFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), true)
            }

            is AssessmentFamilyPlanningFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), false)
            }

            is AssessmentFamilyPlanningSummaryFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), true)
            }

            is AssessmentPregnantWomenRegistrationFragment -> {
                return Pair(fragment.getCurrentAnsweredStatus(), false)
            }

            is AssessmentPregnantWomenRegistrationSummaryFragment -> {
                return Pair(false, true)
            }

            else -> return Pair(false, false)
        }
    }

    private fun navigationHandling(
        isHome: Boolean,
        isFromSummary: Boolean,
    ) {
        if (isFromSummary && !CommonUtils.isNonCommunity()) {
            startBackgroundOfflineSync()
        }

        if (isHome) {
            setupAnalytic(AnalyticsDefinedParams.HomeButtonClicked)
            val intent = Intent(this, LandingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        } else {
            setupAnalytic(AnalyticsDefinedParams.BackButtonClicked)
            when (supportFragmentManager.findFragmentById(R.id.fragmentContainer)) {
                is AssessmentICCMSummaryFragment,
                is AssessmentRMNCHSummaryFragment,
                is AssessmentRMNCHNeonateSummaryFragment,
                is AssessmentOtherSymptomSummaryFragment,
                -> {
                    finishSuccessFlow()
                }
                is AssessmentNCDSummaryFragment -> {
                    val intent = Intent(this, LandingActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    finish()
                }
                else -> {
                    this@AssessmentActivity.finish()
                }
            }
        }
    }

    private fun setupAnalytic(btnClickType: String) {
        val type = when (supportFragmentManager.findFragmentById(R.id.formsFragmentContainer)) {
            is AssessmentICCMFragment -> {
                AnalyticsDefinedParams.ICCMAssessment
            }
            is AssessmentRMNCHFragment -> {
                viewModel.workflowName.plus(AnalyticsDefinedParams.RMNCHAssessment)
            }
            is AssessmentRMNCHNeonateFragment -> {
                AnalyticsDefinedParams.RMNCHNeonateAssessment
            }
            is AssessmentOtherSymptomsFragment -> {
                AnalyticsDefinedParams.OtherSymptoms
            }
            is AssessmentPregnantWomenRegistrationFragment -> {
                AnalyticsDefinedParams.PREGNANT_WOMEN_PROFILE
            }
            else -> {
                ""
            }
        }
        viewModel.setAnalyticsData(
            UserDetail.startDateTime,
            eventType = type,
            exitReason = btnClickType,
            eventName = AnalyticsDefinedParams.AssessmentCreation,
            isCompleted = false,
        )
    }

    private fun loadSummaryFragment() {
        val bundle = Bundle()
        bundle.putString(MenuConstants.WorkFlowName, viewModel.workflowName)
        when (viewModel.menuId) {
            MenuConstants.ICCM_MENU_ID -> {
                setTitle(Summary.capitalizeFirstChar())
                hideBackButton()
                replaceFragmentInId<AssessmentICCMSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentICCMSummaryFragment.TAG,
                )
            }

            MenuConstants.TB_MENU_ID -> {
                setTitle(Summary.capitalizeFirstChar())
                hideBackButton()
                replaceFragmentInId<AssessmentTBSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentTBSummaryFragment.TAG,
                )
            }

            MenuConstants.OTHER_SYMPTOMS -> {
                setTitle(Summary.capitalizeFirstChar())
                hideBackButton()
                replaceFragmentInId<AssessmentOtherSymptomSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentOtherSymptomSummaryFragment::class.simpleName,
                )
            }

            MenuConstants.RMNCH_MENU_ID -> {
                setTitle(Summary.capitalizeFirstChar())
                hideBackButton()
                replaceFragmentInId<AssessmentRMNCHSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    bundle = bundle,
                    tag = AssessmentRMNCHSummaryFragment.TAG,
                )
            }

            MenuConstants.NCD_MENU_ID -> {
                if (CommonUtils.isNonCommunity()) {
                    setTitle(getString(R.string.assessment_summary))
                    showBackButton()
                    replaceFragmentInId<AssessmentNCDSummaryFragment>(
                        binding.formsFragmentContainer.id,
                        tag = AssessmentNCDSummaryFragment.TAG,
                    )
                } else {
                    setTitle(Summary.capitalizeFirstChar())
                    hideBackButton()
                    replaceFragmentInId<AssessmentSLNCDSummaryFragment>(
                        binding.formsFragmentContainer.id,
                        tag = AssessmentSLNCDSummaryFragment.TAG,
                    )
                }
            }

            MenuConstants.MATERNAL_HEALTH -> {
                setTitle(getString(R.string.assessment_summary))
                showBackButton()
                replaceFragmentInId<AssessmentNCDSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentNCDSummaryFragment.TAG,
                )
            }

            MenuConstants.MENTAL_HEALTH -> {
                setTitle(getString(R.string.assessment_summary))
                showBackButton()
                replaceFragmentInId<AssessmentNCDSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentNCDSummaryFragment.TAG,
                )
            }

            MenuConstants.FP_MENU_ID -> {
                setTitle(Summary.capitalizeFirstChar())
                hideBackButton()
                replaceFragmentInId<AssessmentFamilyPlanningSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentFamilyPlanningSummaryFragment::class.simpleName,
                )
            }
            MenuConstants.PREGNANT_WOMEN_PROFILE -> {
                setTitle(getString(R.string.assessment_summary))
                hideBackButton()
                replaceFragmentInId<AssessmentPregnantWomenRegistrationSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentPregnantWomenRegistrationSummaryFragment.TAG,
                )
            }
        }
    }

    private fun loadFragment() {
        val bundle = Bundle()
        bundle.putString(DefinedParams.FhirId, intent.getStringExtra(DefinedParams.FhirId))
        bundle.putString(DefinedParams.ORIGIN, intent.getStringExtra(DefinedParams.ORIGIN))
        bundle.putBoolean(MenuConstants.FOLLOW_UP, intent.getBooleanExtra(MenuConstants.FOLLOW_UP, false))
        when (viewModel.menuId) {
            MenuConstants.ICCM_MENU_ID -> {
                setTitle(MenuConstants.ICCM_MENU_ID.uppercase())
                replaceFragmentInId<AssessmentICCMFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentICCMFragment.TAG,
                )
            }

            MenuConstants.TB_MENU_ID -> {
                bundle.putBoolean(DefinedParams.CONTACT_TRACING, intent.getBooleanExtra(DefinedParams.CONTACT_TRACING, false))
                bundle.putLong(DefinedParams.HouseholdId, viewModel.selectedHouseholdId)
                bundle.putBoolean(DefinedParams.isTbPatient, true)
                setTitle(MenuConstants.TB_MENU_ID.uppercase())
                replaceFragmentInId<AssessmentTBFragment>(
                    binding.formsFragmentContainer.id,
                    bundle = bundle,
                    tag = AssessmentTBFragment.TAG,
                )
            }

            MenuConstants.RMNCH_MENU_ID -> {
                setTitle(MenuConstants.RMNCH_MENU_ID.uppercase())
                replaceFragmentInId<AssessmentRMNCHFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentRMNCHFragment.TAG,
                )
            }

            MenuConstants.OTHER_SYMPTOMS -> {
                setTitle(OtherSymptoms)
                replaceFragmentInId<AssessmentOtherSymptomsFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentOtherSymptomsFragment::class.simpleName,
                )
            }

            MenuConstants.NCD_MENU_ID -> {
                if (CommonUtils.isNonCommunity()) {
                    setTitle(AssessmentDefinedParams.ncd.uppercase())
                    bundle.putString(Screening.type, MenuConstants.NCD_MENU_ID)
                    showLoading()
                    replaceFragmentInId<AssessmentNCDFragment>(
                        binding.formsFragmentContainer.id,
                        bundle = bundle,
                        tag = AssessmentNCDFragment.TAG,
                    )
                } else {
                    setTitle(AssessmentDefinedParams.ncd.uppercase())
                    bundle.putString(Screening.type, MenuConstants.NCD_MENU_ID)
                    showLoading()
                    replaceFragmentInId<AssessmentSLNCDFragment>(
                        binding.formsFragmentContainer.id,
                        bundle = bundle,
                        tag = AssessmentSLNCDFragment.TAG,
                    )
                }
            }

            MenuConstants.MATERNAL_HEALTH -> {
                setTitle(AssessmentDefinedParams.MaternalHealth)
                bundle.putString(Screening.type, MenuConstants.MATERNAL_HEALTH)
                showLoading()
                replaceFragmentInId<AssessmentNCDFragment>(
                    binding.formsFragmentContainer.id,
                    bundle = bundle,
                    tag = AssessmentNCDFragment.TAG,
                )
            }

            MenuConstants.MENTAL_HEALTH -> {
                setTitle(this.getString(R.string.mental_health))
                bundle.putString(Screening.type, MenuConstants.MENTAL_HEALTH)
                showLoading()
                replaceFragmentInId<AssessmentNCDFragment>(
                    binding.formsFragmentContainer.id,
                    bundle = bundle,
                    tag = AssessmentNCDFragment.TAG,
                )
            }

            MenuConstants.FP_MENU_ID -> {
                setTitle(getString(R.string.family_planning).uppercase())
                replaceFragmentInId<AssessmentFamilyPlanningFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentFamilyPlanningFragment.TAG,
                )
            }

            MenuConstants.PREGNANT_WOMEN_PROFILE -> {
                setTitle(getString(R.string.pregnant_women_profile))
                replaceFragmentInId<AssessmentPregnantWomenRegistrationFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentPregnantWomenRegistrationFragment.TAG,
                )
            }
        }
    }

    private fun attachObservers() {
        viewModel.assessmentSaveLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let { assessment ->
                        val detailsJson = JSONObject(assessment.assessmentDetails)
                        val ancObject = detailsJson.optJSONObject(ANC_MENU)
                        val isDeathOfMother = ancObject?.optBoolean(DeathOfMother, false) == true

                        val childHoodObject = detailsJson.optJSONObject(ChildHoodVisit)
                        val isDeathOfNeonate = childHoodObject?.optBoolean(deathOfBaby, false) == true
                        if (CommonUtils.isCommunity() && (isDeathOfMother || isDeathOfNeonate)) {
                            viewModel.workflowName?.let {
                                startCbsActivity(
                                    workFlowName = it,
                                    memberId = viewModel.selectedHouseholdMemberId,
                                    assessmentId = viewModel.assessmentSaveLiveData.value
                                        ?.data
                                        ?.id,
                                    deathOfMother = isDeathOfMother,
                                    deathOfNewborn = isDeathOfNeonate,
                                )
                            }
                        } else {
                            loadSummaryFragment()
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }

        viewModel.assessmentUpdateLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.SUCCESS -> {
                    hideLoading()
                    if (!viewModel.isCbs) {
                        finishSuccessFlow()
                        if (!CommonUtils.isNonCommunity()) {
                            startBackgroundOfflineSync()
                        }
                    } else {
                        viewModel.isCbs = false
                        startCbsActivity(
                            workFlowName = PNCNeonatal,
                            memberId = assessmentRMNCHNeonateViewModel.childId,
                            assessmentId = assessmentRMNCHNeonateViewModel.assessmentSaveLiveData.value
                                ?.data
                                ?.second
                                ?.id,
                            deathOfNewborn = true,
                            motherId = viewModel.memberDetailsLiveData.value
                                ?.data
                                ?.id,
                        )
                    }
                }

                else -> {}
            }
        }

        viewModel.assessmentSaveResponse.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        showErrorDialogue(
                            getString(R.string.error),
                            it,
                            isNegativeButtonNeed = false,
                        ) {}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    loadSummaryFragment()
                }
            }
        }

        viewModel.saveRxBuddyDetails.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        showErrorDialogue(
                            getString(R.string.error),
                            it,
                            isNegativeButtonNeed = false,
                        ) {}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    setTitle(Summary.capitalizeFirstChar())
                    hideBackButton()
                    replaceFragmentInId<RxBuddySummaryFragment>(
                        binding.formsFragmentContainer.id,
                        tag = RxBuddySummaryFragment.TAG,
                    )
                }
            }
        }

        viewModel.saveRxBuddyFollowUpLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }
                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        showErrorDialogue(
                            getString(R.string.error),
                            it,
                            isNegativeButtonNeed = false,
                        ) {}
                    }
                }
                ResourceState.SUCCESS -> {
                    hideLoading()
                    val bundle = Bundle().apply {
                        putBoolean(DefinedParams.isRxBuddyFollowUp, true)
                        putBoolean(MenuConstants.FOLLOW_UP, intent.getBooleanExtra(MenuConstants.FOLLOW_UP, false))
                    }
                    setTitle(Summary.capitalizeFirstChar())
                    hideBackButton()
                    replaceFragmentInId<RxBuddySummaryFragment>(
                        binding.formsFragmentContainer.id,
                        bundle = bundle,
                        tag = RxBuddySummaryFragment.TAG,
                    )
                }
            }
        }
    }

    private fun startCbsActivity(
        workFlowName: String,
        memberId: Long?,
        assessmentId: Long?,
        deathOfMother: Boolean = false,
        deathOfNewborn: Boolean = false,
        motherId: Long? = null,
    ) {
        val intent = Intent(this, CbsActivity::class.java).apply {
            putExtra(DefinedParams.MemberID, memberId)
            putExtra(DefinedParams.DOB, viewModel.selectedMemberDob)
            putExtra(MenuConstants.WorkFlowName, workFlowName)
            putExtra(DefinedParams.MenuId, DefinedParams.CBS.lowercase())
            putExtra(DefinedParams.MOTHER_ID, motherId)
            if (assessmentId != null) putExtra(DefinedParams.AssessmentId, assessmentId)
            if (deathOfMother) putExtra(DeathOfMother, true)
            if (deathOfNewborn) putExtra(RMNCH.deathOfNewborn, true)
        }
        startActivity(intent)
    }

    private fun finishSuccessFlow() {
        val intent = if (viewModel.followUpId != null) {
            Intent(this, FollowUpMyPatientActivity::class.java)
        } else {
            Intent(this, HouseholdSearchActivity::class.java)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun getIntentValue() {
        viewModel.selectedHouseholdMemberId = intent.getLongExtra(DefinedParams.MemberID, -1L)
        viewModel.menuId = intent.getStringExtra(DefinedParams.MenuId)
        viewModel.workflowName = intent.getStringExtra(MenuConstants.WorkFlowName)
        viewModel.memberFhirId = intent.getStringExtra(DefinedParams.FhirId)
        viewModel.selectedMemberDob = intent.getStringExtra(DefinedParams.DOB)
        viewModel.selectedHouseholdId = intent.getLongExtra(DefinedParams.HouseholdId, -1L)
        val followUpId = intent.getLongExtra(DefinedParams.FollowUpId, -1L)
        if (followUpId != -1L) {
            viewModel.followUpId = followUpId
        } else {
            viewModel.followUpId = null
        }

        viewModel.workflowName?.let {
            viewModel.setUserJourney(getUserJourneyName(it))
        }
    }

    private fun getUserJourneyName(it: String): String =
        if (it == PNC) {
            AnalyticsDefinedParams.PNCMOTHERASSESSMENT
        } else if (it == ChildHoodVisit) {
            AnalyticsDefinedParams.RMNCHCHILDASSESSMENT
        } else {
            it.plus(getString(R.string.assessment))
        }

    fun replaceAssessmentRMNCHNeonateFragment() {
        replaceFragmentInId<AssessmentRMNCHNeonateFragment>(
            binding.formsFragmentContainer.id,
            tag = AssessmentRMNCHNeonateFragment::class.simpleName,
        )
    }

    fun replaceAssessmentRMNCHNeonateSummaryFragment() {
        hideBackButton()
        replaceFragmentInId<AssessmentRMNCHNeonateSummaryFragment>(
            binding.formsFragmentContainer.id,
            tag = AssessmentRMNCHNeonateSummaryFragment::class.simpleName,
        )
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backNavigation(false)
            }
        }

    override fun onResume() {
        super.onResume()
        getCurrentLocation()
    }
}
