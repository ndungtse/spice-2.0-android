package com.medtroniclabs.spice.ui.assessment

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
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
import com.medtroniclabs.spice.ui.assessment.referrallogic.utils.ReferralStatus
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import dagger.hilt.android.AndroidEntryPoint

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
            callback = {
                backNavigation()
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

    private fun backNavigation() {
        if (viewModel.isInputUpdated){
            showErrorDialogue(
                getString(R.string.alert),
                getString(R.string.exit_reason),
                isNegativeButtonNeed = true
            ) { isPositive ->
                if (isPositive) {
                    this@AssessmentActivity.finish()
                }
            }
        } else {
            this@AssessmentActivity.finish()
        }
    }

    private fun loadSummaryFragment() {
        when (viewModel.menuId) {
            MenuConstants.ICCM_MENU_ID -> {
                setTitle(Summary.capitalizeFirstChar())
                hideBackButton()
                viewModel.isInputUpdated = false
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
                viewModel.isInputUpdated = false
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
                viewModel.isInputUpdated = true
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
                viewModel.isInputUpdated = true
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
                        insertOtherAssessmentDetails()
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
                    if (viewModel.isDismiss) {
                        finish()
                    }
                }

                else -> {}
            }
        }
    }

    private fun insertOtherAssessmentDetails() {
        when (viewModel.referralStatus) {
            ReferralStatus.Referred.name -> {
                viewModel.nearestFacilityLiveData.value?.data?.let { siteList ->
                    val item = siteList.filter { it.isDefault }
                    if (item.isNotEmpty()) {
                        viewModel.otherAssessmentDetails[AssessmentDefinedParams.ReferredPHUSite] = item[0].name
                        viewModel.otherAssessmentDetails[AssessmentDefinedParams.ReferredPHUSiteID] =
                            item[0].fhirId?.toLong() ?: item[0].id
                    }
                }
                viewModel.updateOtherAssessmentDetails()
            }

            ReferralStatus.OnTreatment.name -> {
                val treatmentDate = DateUtils.getDateAfterDays(viewModel.referralReason?.mapNotNull { viewModel.treatmentDays[it] }?.minOrNull() ?: 3)
                viewModel.otherAssessmentDetails[AssessmentDefinedParams.NextFollowupDate] =
                    DateUtils.convertDateTimeToDate(
                        treatmentDate,
                        DateUtils.DATE_ddMMyyyy,
                        DateUtils.DATE_FORMAT_yyyyMMddHHmmssZZZZZ
                    )
                viewModel.updateOtherAssessmentDetails()
            }
        }
    }

    private fun getIntentValue() {
        viewModel.selectedHouseholdMemberId = intent.getLongExtra(DefinedParams.MemberID, -1L)
        viewModel.menuId = intent.getStringExtra(DefinedParams.MenuId)
        viewModel.workflowName = intent.getStringExtra(MenuConstants.WorkFlowName)
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
                backNavigation()
            }
        }
}