package com.medtroniclabs.spice.ui.cbs.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.postError
import com.medtroniclabs.spice.appextensions.startBackgroundOfflineSync
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.AssessmentId
import com.medtroniclabs.spice.databinding.ActivityAssessmentBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.medtroniclabs.spice.ui.cbs.fragment.CbsFragment
import com.medtroniclabs.spice.ui.cbs.fragment.CbsSummaryFragment
import com.medtroniclabs.spice.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CbsActivity : BaseActivity() {
    private lateinit var binding: ActivityAssessmentBinding
    private val viewModel: AssessmentViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityAssessmentBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.cbs_register),
            homeAndBackVisibility = Pair(true, true),
            callback = {
                navigateHome()
            },
            callbackHome = {
                navigateHome(true)
            },
        )
        initView()
        attachObservers()
    }
    private fun backNavigation(): Pair<Boolean, Boolean> {
        val fragment = supportFragmentManager.findFragmentById(R.id.formsFragmentContainer)
        return if (fragment is CbsFragment) {
            Pair(fragment.getCurrentAnsweredStatus(), false)
        } else if (fragment is CbsSummaryFragment) {
            Pair(true, true)
        } else {
            Pair(false, false)
        }
    }

    private fun navigateHome(isHome: Boolean = false) {
        val backButtonStatus = backNavigation()
        if (backButtonStatus.first) {
            showErrorDialogue(
                getString(R.string.alert),
                getString(R.string.exit_reason),
                isNegativeButtonNeed = true
            ) { isPositive ->
                if (isPositive) {
                    navigationHandling(isHome, backButtonStatus.second)
                }
            }
        } else {
            finish()
        }
    }

    private fun navigationHandling(isHome: Boolean, isFromSummary: Boolean) {
        if (isFromSummary)
            startBackgroundOfflineSync()

        if (isHome) {
            val intent = Intent(this, LandingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        } else {
            this.finish()
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
        viewModel.callResultSaveLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let { it ->
                        // insertOtherAssessmentDetails()
                        viewModel.assessmentSaveLiveData.postValue(resource)
                        viewModel.callResultSaveLiveData.postError()
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    private fun loadSummaryFragment() {
        setTitle(AssessmentDefinedParams.Summary.capitalizeFirstChar())
        hideBackButton()
        replaceFragmentInId<CbsSummaryFragment>(
            binding.formsFragmentContainer.id,
            tag = CbsSummaryFragment.TAG
        )
    }

    private fun initView() {
        loadFragment()
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        }

    private fun loadFragment() {
        viewModel.selectedHouseholdMemberId = intent.getLongExtra(DefinedParams.MemberID, -1L)
        viewModel.menuId = intent.getStringExtra(DefinedParams.MenuId)
        viewModel.workflowName = intent.getStringExtra(MenuConstants.WorkFlowName)
        val bundle = Bundle()
        bundle.putString(DefinedParams.FhirId, intent.getStringExtra(DefinedParams.FhirId))
        bundle.putString(DefinedParams.ORIGIN, intent.getStringExtra(DefinedParams.ORIGIN))
        bundle.putString(MenuConstants.WorkFlowName, intent.getStringExtra(MenuConstants.WorkFlowName))
        bundle.putLong(AssessmentId, intent.getLongExtra(AssessmentId, 0L))
        setTitle(getString(R.string.cbs_register))
        replaceFragmentInId<CbsFragment>(
            binding.formsFragmentContainer.id,
            bundle = bundle,
            tag = CbsFragment.TAG,
        )
    }

}
