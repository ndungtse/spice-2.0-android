package com.medtroniclabs.spice.ui.assessment

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityAssessmentBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.OtherSymptoms
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams.Summary
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentICCMFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentICCMSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentRMNCHFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentOtherSymptomSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentOtherSymptomsFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentRMNCHSummaryFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentTBFragment
import com.medtroniclabs.spice.ui.assessment.fragment.AssessmentTBSummaryFragment
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.medtroniclabs.spice.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentActivity : BaseActivity() {
    private lateinit var binding: ActivityAssessmentBinding
    private val viewModel: AssessmentViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssessmentBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.assessment)
        )
        getIntentValue()
        loadFragment()
        attachObservers()
    }

    private fun loadSummaryFragment() {
        when (viewModel.menuId) {
            MenuConstants.ICCM_MENU_ID -> {
                setTitle(Summary.capitalizeFirstChar())
                replaceFragmentInId<AssessmentICCMSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentICCMSummaryFragment.TAG
                )
            }

            MenuConstants.TB_MENU_ID -> {
                setTitle(Summary.capitalizeFirstChar())
                replaceFragmentInId<AssessmentTBSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentTBSummaryFragment.TAG
                )
            }

            MenuConstants.OTHER_SYMPTOMS -> {
                setTitle(Summary.capitalizeFirstChar())
                replaceFragmentInId<AssessmentOtherSymptomSummaryFragment>(
                    binding.formsFragmentContainer.id,
                    tag = AssessmentOtherSymptomSummaryFragment::class.simpleName
                )
            }

            MenuConstants.RMNCH_MENU_ID -> {
                setTitle(Summary.capitalizeFirstChar())
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
                    finish()
                }

                else -> {}
            }
        }
    }

    private fun getIntentValue() {
        viewModel.selectedHouseholdMemberId = intent.getLongExtra(DefinedParams.MemberID, -1L)
        viewModel.menuId = intent.getStringExtra(DefinedParams.MenuId)
        viewModel.workflowName = intent.getStringExtra(MenuConstants.WorkFlowName)
    }


}