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
import com.medtroniclabs.spice.common.DefinedParams.CBS
import com.medtroniclabs.spice.databinding.ActivityAssessmentBinding
import com.medtroniclabs.spice.formgeneration.extension.capitalizeFirstChar
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.MenuConstants
import com.medtroniclabs.spice.ui.assessment.AssessmentDefinedParams
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.DeathOfMother
import com.medtroniclabs.spice.ui.assessment.rmnch.RMNCH.deathOfNewborn
import com.medtroniclabs.spice.ui.assessment.viewmodel.AssessmentViewModel
import com.medtroniclabs.spice.ui.cbs.fragment.CbsFragment
import com.medtroniclabs.spice.ui.cbs.fragment.CbsMemberRegistration
import com.medtroniclabs.spice.ui.cbs.fragment.CbsSummaryFragment
import com.medtroniclabs.spice.ui.household.HouseholdSearchActivity
import com.medtroniclabs.spice.ui.landing.LandingActivity
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class CbsActivity : BaseActivity(), OnDialogDismissListener {
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
                handleBack()
            },
            callbackHome = {
                if (intent.getBooleanExtra(DeathOfMother, false)) {
                    navigateAncCbs()
                } else {
                    navigateHome(true)
                }
            },
        )
        initView()
        attachObservers()
    }

    fun handleBack() {
        if (intent.getBooleanExtra(DeathOfMother, false)) {
            navigateAncCbs()
        } else {
            navigateHome()
        }
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        finishSuccessFlow()
    }

    private fun finishSuccessFlow() {
        val intent = Intent(this, HouseholdSearchActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        this.finish()
        this.startBackgroundOfflineSync()
    }

    private fun backNavigation(): Pair<Boolean, Boolean> {
        val fragment = supportFragmentManager.findFragmentById(R.id.formsFragmentContainer)
        return if (fragment is CbsFragment) {
            Pair(fragment.getCurrentAnsweredStatus(), false)
        } else if (fragment is CbsSummaryFragment) {
            Pair(true, true)
        } else if (fragment is CbsMemberRegistration){
            Pair(fragment.getCurrentAnsweredStatus(), false)
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
            navigationHandling(isHome, backButtonStatus.second)
        }
    }

    private fun navigateAncCbs() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                navigationHandling(isHome = true, true)
            }
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
                    resource.data?.let {
                        // insertOtherAssessmentDetails()
                        if (intent.getBooleanExtra(deathOfNewborn, false)
                            || intent.getBooleanExtra(DeathOfMother, false)
                        ) {
                            val key = if (intent.getBooleanExtra(DeathOfMother, false)) {
                                DefinedParams.RmnchNotifiableCondition
                            } else if (intent.getBooleanExtra(deathOfNewborn, false)) {
                                DefinedParams.CbsNotifiableCondition
                            } else {
                                DefinedParams.CbsNotifiableCondition
                            }
                            viewModel.updateMemberDeceasedStatus(
                                viewModel.memberDetailsLiveData.value?.data?.id ?: -1L, false,
                                extractNotifiableConditions(viewModel.assessmentStringLiveData.value,key)
                            )
                        }
                        loadSummaryFragment()
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
        viewModel.birthLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let {
                        loadMemberRegistration()
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
                    resource.data?.let {
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
        viewModel.memberCbsDetailsLiveData.observe(this) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resource.data?.let {
                        // insertOtherAssessmentDetails()
                        val deathReason =
                            ((viewModel.assessmentMap[CBS.lowercase()] as? Map<String, Any>)
                                ?.get(DefinedParams.surveillanceDetails) as? Map<String, Any>)
                                ?.get(DefinedParams.RmnchNotifiableCondition) as? List<Map<String, Any>>

                        val rmnchText =
                            deathReason?.mapNotNull { it[DefinedParams.NAME] as? String }
                                ?.toMutableList() ?: mutableListOf()

                        val otherText =
                            ((viewModel.assessmentMap[CBS.lowercase()] as? Map<String, Any>)
                                ?.get(DefinedParams.surveillanceDetails) as? Map<String, Any>)
                                ?.get(DefinedParams.OtherNotifiableConditions) as? String

                        val index = rmnchText.indexOfFirst {
                            it.equals(
                                DefinedParams.Other,
                                ignoreCase = true
                            )
                        }

                        if (index != -1 && !otherText.isNullOrBlank()) {
                            rmnchText[index] = "${DefinedParams.Other} ($otherText)"
                        }
                        val finalText = rmnchText.joinToString(", ")

                        val isAncOrNormal = viewModel.birthLiveData.value?.data?.third ?: false
                        if (isAncOrNormal) {
                            val isDelete = viewModel.birthLiveData.value?.data?.second ?: false
                            if (isDelete) {
                                viewModel.updateMemberDeceasedStatus(
                                    viewModel.memberDetailsLiveData.value?.data?.id ?: -1L, false,
                                    finalText
                                )
                            }
                            viewModel.saveAssessment(
                                viewModel.assessmentMap,
                                viewModel.referralResult,
                                viewModel.menuId
                            )
                        } else {
                            viewModel.assessment?.let {
                                viewModel.saveCallResult(
                                    it,
                                    viewModel.resultValue
                                )
                            }
                        }
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }
    private fun extractNotifiableConditions(jsonString: String?,key :String): String {
        return try {
            jsonString?.let {
                val jsonObject = JSONObject(it)
                val surveillanceDetails = jsonObject.getJSONObject(CBS.lowercase())
                    .getJSONObject(DefinedParams.surveillanceDetails)

                val jsonArray =
                    surveillanceDetails.getJSONArray(key)

                val conditions = (0 until jsonArray.length()).mapNotNull { index ->
                    jsonArray.getJSONObject(index).optString(DefinedParams.NAME, null)
                }.toMutableList()

                // Check if "Other" exists, then append "Other(value)"
                if (DefinedParams.Other in conditions) {
                    val otherValue =
                        surveillanceDetails.optString(DefinedParams.OtherNotifiableConditions, "")
                            .takeIf { it.isNotEmpty() }
                    if (otherValue != null) {
                        conditions[conditions.indexOf(DefinedParams.Other)] =
                            "${DefinedParams.Other}($otherValue)"
                    }
                }
                conditions.joinToString(", ")
            } ?: ""
        } catch (e: Exception) {
            "" // Return an empty string if parsing fails
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

    private fun loadMemberRegistration() {
        setTitle(getString(R.string.member_registration))
        replaceFragmentInId<CbsMemberRegistration>(
            binding.formsFragmentContainer.id,
            tag = CbsMemberRegistration.TAG
        )
    }

    private fun initView() {
        loadFragment()
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        }

    private fun loadFragment() {
        viewModel.selectedHouseholdMemberId = intent.getLongExtra(DefinedParams.MemberID, -1L)
        viewModel.menuId = intent.getStringExtra(DefinedParams.MenuId)
        viewModel.workflowName = intent.getStringExtra(MenuConstants.WorkFlowName)
        viewModel.motherID = intent.getLongExtra(DefinedParams.MOTHER_ID, -1L)
        val bundle = Bundle()
        bundle.putString(DefinedParams.FhirId, intent.getStringExtra(DefinedParams.FhirId))
        bundle.putString(DefinedParams.ORIGIN, intent.getStringExtra(DefinedParams.ORIGIN))
        bundle.putString(
            MenuConstants.WorkFlowName,
            intent.getStringExtra(MenuConstants.WorkFlowName)
        )
        bundle.putLong(AssessmentId, intent.getLongExtra(AssessmentId, 0L))
        bundle.putBoolean(deathOfNewborn, intent.getBooleanExtra(deathOfNewborn, false))
        bundle.putBoolean(DeathOfMother, intent.getBooleanExtra(DeathOfMother, false))
        setTitle(getString(R.string.cbs_register))
        replaceFragmentInId<CbsFragment>(
            binding.formsFragmentContainer.id,
            bundle = bundle,
            tag = CbsFragment.TAG,
        )
    }

}
