package com.medtroniclabs.spice.ui.household

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.model.UserDetail
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.startBackgroundOfflineSync
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.DefinedParams.FhirMemberID
import com.medtroniclabs.spice.common.DefinedParams.MemberID
import com.medtroniclabs.spice.common.DefinedParams.VillageId
import com.medtroniclabs.spice.common.DefinedParams.isCreateHouseholdForPhu
import com.medtroniclabs.spice.common.DefinedParams.isMemberRegistration
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.databinding.ActivityHouseholdRegistrationBinding
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.household.HouseholdDefinedParams.isPhuWalkInsFlow
import com.medtroniclabs.spice.ui.household.fragment.HouseHoldRegistrationFragment
import com.medtroniclabs.spice.ui.household.summary.HouseholdSummaryActivity
import com.medtroniclabs.spice.ui.household.viewmodel.HouseRegistrationViewModel
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import com.medtroniclabs.spice.ui.member.MemberRegistrationFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HouseholdActivity : BaseActivity(), OnDialogDismissListener {

    private lateinit var binding: ActivityHouseholdRegistrationBinding

    private val householdRegistrationViewModel: HouseRegistrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityHouseholdRegistrationBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.household_registration),
            callback = {
                if (validateFormInputs()){
                    backNavigation()
                } else {
                    logExitEventAnalytics()
                }
            }
        )

        intent.getStringExtra(DefinedParams.KeySignature)?.let {
            householdRegistrationViewModel.signatureFilename = it
        }

        intent.getStringExtra(DefinedParams.KeyInitial)?.let {
            householdRegistrationViewModel.initialValue = it
        }

        UserDetail.eventName=AnalyticsDefinedParams.HouseholdCreation
        initializeView()
        attachObserver()
       // phuMemberRegistration()
    }

    private fun validateFormInputs(): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (fragment is HouseHoldRegistrationFragment) {
            return fragment.getHouseHoldEnteredInputs()
        }
        else if (fragment is MemberRegistrationFragment){
            return fragment.getEnteredInputs()
        }
        return false
    }

    private fun backNavigation() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                logExitEventAnalytics()
            }
        }
    }

    private fun logExitEventAnalytics() {
        householdRegistrationViewModel.setAnalyticsData(
            UserDetail.startDateTime,
            eventType = AnalyticsDefinedParams.HouseholdCreation,
            eventName = householdRegistrationViewModel.eventName,
            exitReason = AnalyticsDefinedParams.BackButtonClicked,
            isCompleted = false
        )
        this@HouseholdActivity.finish()
    }

    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(this)
        locationManager.getCurrentLocation {
            householdRegistrationViewModel.setCurrentLocation(it)
        }
    }

    private fun initializeView() {
        val householdId = intent.getLongExtra(HouseholdDefinedParams.ID, -1L)
        val isMemberRegistration = intent.getBooleanExtra(isMemberRegistration, false)
        val memberId = intent.getLongExtra(MemberID, -1L)
        val memberFhirId = intent.getLongExtra(FhirMemberID,-1L)
        val isCreateHouseholdForPhu = intent.getBooleanExtra(isCreateHouseholdForPhu, false)
        val isPhuWalkInsFlow = intent.getBooleanExtra(isPhuWalkInsFlow, false)

        householdRegistrationViewModel.isMemberRegistration = isMemberRegistration
        householdRegistrationViewModel.householdId = householdId
        householdRegistrationViewModel.memberID = memberId
        householdRegistrationViewModel.isCreateHouseholdForPhu = isCreateHouseholdForPhu
        householdRegistrationViewModel.isPhuWalkInsFlow = isPhuWalkInsFlow

        if (isCreateHouseholdForPhu) {
            val arguments = Bundle()
            val vId = intent.getLongExtra(VillageId, -1L)
            if (vId != -1L)
                arguments.putLong(VillageId, vId)
            launchHouseholdRegistration(arguments)
        } else if (isMemberRegistration || memberId != -1L) {
            val arguments = Bundle().apply {
                putBoolean(AnalyticsDefinedParams.AddNewMember, true)
                putBoolean(HouseholdDefinedParams.isPhuWalkInsFlow, isPhuWalkInsFlow)
                putLong(MemberID, memberId)
                putLong(FhirMemberID, memberFhirId)
                putString(
                    AnalyticsDefinedParams.StartDate,
                    UserDetail.startDateTime
                )
            }
            launchMemberRegistrationFragment(arguments)
        } else {
            launchHouseholdRegistration(Bundle())
        }
    }

    private fun launchHouseholdRegistration(args: Bundle) {
        setTitle(getString(R.string.household_registration))
        replaceFragmentInId<HouseHoldRegistrationFragment>(
            binding.fragmentContainer.id,
            bundle = args,
            tag = HouseHoldRegistrationFragment::class.simpleName
        )
    }

    private fun launchMemberRegistrationFragment(arg: Bundle) {
        setTitle(getString(R.string.member_registration))
        replaceFragmentInId<MemberRegistrationFragment>(
            binding.fragmentContainer.id,
            bundle = arg,
            tag = MemberRegistrationFragment::class.simpleName
        )
    }

    private fun launchHouseholdSummaryPage() {
        val intent = Intent(this, HouseholdSummaryActivity::class.java)
        intent.putExtra(
            HouseholdDefinedParams.ID,
            householdRegistrationViewModel.householdId
        )
        intent.putExtra(HouseholdDefinedParams.isFromHouseHoldRegistration, false)
        startActivity(intent)
        finish()
    }

    private fun attachObserver() {
        householdRegistrationViewModel.houseHoldRegistrationLiveData.observe(this@HouseholdActivity) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    if (householdRegistrationViewModel.householdId != -1L)
                        launchHouseholdSummaryPage()
                    else {
                        val memberId = intent.getLongExtra(MemberID, -1L)
                        val memberFhirId = intent.getLongExtra(FhirMemberID,-1L)
                        val isPhuWalkInsFlow = intent.getBooleanExtra(isPhuWalkInsFlow, false)
                        val arguments = Bundle().apply {
                            putBoolean(AnalyticsDefinedParams.AddNewMember, true)
                            putBoolean(HouseholdDefinedParams.isPhuWalkInsFlow, isPhuWalkInsFlow)
                            putLong(MemberID, memberId)
                            putLong(FhirMemberID, memberFhirId)
                            putString(
                                AnalyticsDefinedParams.StartDate,
                                UserDetail.startDateTime
                            )
                        }
                        // Check if this is the first member (household head registration)
                        val isFirstMember = !householdRegistrationViewModel.isMemberRegistration && 
                                            memberId == -1L &&
                                            householdRegistrationViewModel.householdEntityDetail != null
                        if (isFirstMember) {
                            setTitle(getString(R.string.household_head_registration))
                        }
                        launchMemberRegistrationFragment(arguments)
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }

        householdRegistrationViewModel.houseHoldUpdateLiveData.observe(this@HouseholdActivity) { resource ->
            when (resource.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    startBackgroundOfflineSync()
                    launchHouseholdSummaryPage()
                }

                ResourceState.ERROR -> {
                    hideLoading()
                }
            }
        }
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        finish()
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backNavigation()
            }
        }

    override fun onResume() {
        super.onResume()
        getCurrentLocation()
    }
}