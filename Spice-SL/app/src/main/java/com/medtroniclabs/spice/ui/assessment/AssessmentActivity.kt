package com.medtroniclabs.spice.ui.assessment

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import android.util.Log
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.app.analytics.utils.CommonUtils
import com.medtroniclabs.spice.app.analytics.utils.UserDetail
import com.medtroniclabs.spice.common.DateUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.databinding.ActivityAssessmentBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.OtherSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Summary
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentICCMFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentICCMSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentOtherSymptomSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentOtherSymptomsFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentRMNCHFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentRMNCHNeonateFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentRMNCHNeonateSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentRMNCHSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentTBFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentTBSummaryFragment
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.medtroniclabs.spice.ui.followup.FollowUpMyPatientActivity
import com.medtroniclabs.spice.ui.household.HouseholdSearchActivity
import com.medtroniclabs.spice.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AssessmentActivity : BaseActivity() {

    private lateinit var binding: ActivityAssessmentBinding
    private val viewModel: AssessmentViewModel by viewModels()

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
                backNavigation(true)
            }

        )
        getIntentValue()
        loadFragment()
        attachObservers()
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(this)
        locationManager.getCurrentLocation {
            viewModel.setCurrentLocation(it)
        }
    }

    private fun backNavigation(isHome: Boolean) {
        if (getBackButtonStatus()) {
            showErrorDialogue(
                getString(R.string.alert),
                getString(R.string.exit_reason),
                isNegativeButtonNeed = true
            ) { isPositive ->
                if (isPositive) {
                    viewModel.setAnalyticsData(
                        UserDetail.startDateTime,
                        exitReason = AnalyticsDefinedParams.BackButtonClicked,
                        eventName = AnalyticsDefinedParams.AssessmentCreation,
                        isCompleted = false
                    )
                    navigationHandling(isHome)
                }
            }
        } else {
            navigationHandling(isHome)
        }
    }

    private fun getBackButtonStatus(): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.formsFragmentContainer)
        if (fragment is AssessmentRMNCHFragment) {
            return fragment.getCurrentAnsweredStatus()
        } else if (fragment is AssessmentICCMFragment) {
            return fragment.getCurrentAnsweredStatus()
        }else if (fragment is AssessmentOtherSymptomsFragment) {
            return fragment.getCurrentAnsweredStatus()
        }else if (fragment is AssessmentRMNCHNeonateFragment) {
            return fragment.getCurrentAnsweredStatus()
        }else if (fragment is AssessmentICCMSummaryFragment){
            return fragment.getCurrentAnsweredStatus()
        }else if (fragment is AssessmentOtherSymptomSummaryFragment) {
            return fragment.getCurrentAnsweredStatus()
        }else if (fragment is AssessmentRMNCHSummaryFragment) {
            return fragment.getCurrentAnsweredStatus()
        }else if (fragment is AssessmentRMNCHNeonateSummaryFragment) {
            return fragment.getCurrentAnsweredStatus()
        }
        return false
    }

    private fun navigationHandling(isHome: Boolean) {
        if (isHome) {
            val intent = Intent(this, LandingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        } else {
            when (supportFragmentManager.findFragmentById(R.id.fragmentContainer)) {
                is AssessmentICCMSummaryFragment,
                is AssessmentRMNCHSummaryFragment,
                is AssessmentRMNCHNeonateSummaryFragment,
                is AssessmentOtherSymptomSummaryFragment -> {
                    finishSuccessFlow()
                }
                else -> {
                    this@AssessmentActivity.finish()
                }
            }
        }
    }

    private fun loadSummaryFragment() {
        when (viewModel.menuId) {
            MenuConstants.ICCM_MENU_ID -> {
                setTitle(Summary.capitalizeFirstChar())
                hideBackButton()
                replaceFragmentInId<AssessmentICCMSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentICCMSummaryFragment.TAG
                )
            }

            MenuConstants.TB_MENU_ID -> {
                setTitle(Summary.capitalizeFirstChar())
                hideBackButton()
                replaceFragmentInId<AssessmentTBSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentTBSummaryFragment.TAG
                )
            }

            MenuConstants.OTHER_SYMPTOMS -> {
                setTitle(Summary.capitalizeFirstChar())
                hideBackButton()
                replaceFragmentInId<AssessmentOtherSymptomSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentOtherSymptomSummaryFragment::class.simpleName
                )
            }

            MenuConstants.RMNCH_MENU_ID -> {
                setTitle(Summary.capitalizeFirstChar())
                hideBackButton()
                replaceFragmentInId<AssessmentRMNCHSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentRMNCHSummaryFragment.TAG
                )
            }
        }
    }

    private fun loadFragment() {
        when (viewModel.menuId) {
            MenuConstants.ICCM_MENU_ID -> {
                setTitle(MenuConstants.ICCM_MENU_ID.uppercase())
                replaceFragmentInId<AssessmentICCMFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentICCMFragment.TAG
                )
            }

            MenuConstants.TB_MENU_ID -> {
                setTitle(MenuConstants.TB_MENU_ID.uppercase())
                replaceFragmentInId<AssessmentTBFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentTBFragment.TAG
                )
            }

            MenuConstants.RMNCH_MENU_ID -> {
                setTitle(MenuConstants.RMNCH_MENU_ID.uppercase())
                replaceFragmentInId<AssessmentRMNCHFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentRMNCHFragment.TAG
                )
            }

            MenuConstants.OTHER_SYMPTOMS -> {
                setTitle(OtherSymptoms)
                replaceFragmentInId<AssessmentOtherSymptomsFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentOtherSymptomsFragment::class.simpleName
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
                    resource.data?.let { _ ->
                        // insertOtherAssessmentDetails()
                        loadSummaryFragment()
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
                    finishSuccessFlow()
                }

                else -> {}
            }
        }
    }

    private fun finishSuccessFlow() {
        val intent = if (viewModel.followUpId != null)
            Intent(this, FollowUpMyPatientActivity::class.java)
        else
            Intent(this, HouseholdSearchActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun getIntentValue() {
        viewModel.selectedHouseholdMemberId = intent.getLongExtra(DefinedParams.MemberID, -1L)
        viewModel.menuId = intent.getStringExtra(DefinedParams.MenuId)
        viewModel.workflowName = intent.getStringExtra(MenuConstants.WorkFlowName)
        val followUpId = intent.getLongExtra(DefinedParams.FollowUpId, -1L)
        if (followUpId != -1L)
            viewModel.followUpId = followUpId
        else
            viewModel.followUpId = null

        viewModel.setUserJourney(viewModel.workflowName?.plus(getString(R.string.assessment)) ?:getString(R.string.assessment))
    }

    fun replaceAssessmentRMNCHNeonateFragment() {
        replaceFragmentInId<AssessmentRMNCHNeonateFragment>(
            binding.formsFragmentContainer.id,
            tag = AssessmentRMNCHNeonateFragment::class.simpleName
        )
    }

    fun replaceAssessmentRMNCHNeonateSummaryFragment() {
        hideBackButton()
        replaceFragmentInId<AssessmentRMNCHNeonateSummaryFragment>(
            binding.formsFragmentContainer.id,
            tag = AssessmentRMNCHNeonateSummaryFragment::class.simpleName
        )
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backNavigation(false)
            }
        }


}