package com.medtroniclabs.spice.ui.medicalreview.addnewmember

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.common.SpiceLocationManager
import com.medtroniclabs.spice.databinding.ActivityAddNewMemberBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.dialog.SuccessDialogFragment
import com.medtroniclabs.spice.ui.household.viewmodel.HouseRegistrationViewModel
import com.medtroniclabs.spice.ui.landing.OnDialogDismissListener
import com.medtroniclabs.spice.ui.medicalreview.utils.MedicalReviewDefinedParams
import com.medtroniclabs.spice.ui.member.MemberRegistrationFragment

class AddNewMemberActivity : BaseActivity(), View.OnClickListener, OnDialogDismissListener {
    private lateinit var binding: ActivityAddNewMemberBinding

    private val householdRegistrationViewModel: HouseRegistrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewMemberBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            true,
            getString(R.string.member_registration),
            homeAndBackVisibility = Pair(true, true),
            callback = {
                if (validateFormInputs()){
                    backNavigation()
                } else {
                    onBackPressPopStack()
                }
            },
            callbackHome = {
                backNavigationToHome()
            }
        )
        initializeView()
    }

    private fun initializeView() {
        binding.btnNext.text = getString(R.string.submit)
        binding.btnCancel.invisible()
        binding.btnNext.safeClickListener(this@AddNewMemberActivity)
        val args = Bundle().apply {
            putBoolean(MedicalReviewDefinedParams.MEDICAL_REVIEW_ADD_MEMBER, true)
        }
        replaceFragmentInId<MemberRegistrationFragment>(
            binding.fragmentContainer.id,
            bundle = args,
            tag = MemberRegistrationFragment::class.simpleName
        )
        supportFragmentManager.setFragmentResultListener(
            MedicalReviewDefinedParams.MEMBER_REG,
            this
        ) { _, _ ->
            SuccessDialogFragment.newInstance(isMember = true).show(supportFragmentManager, SuccessDialogFragment.TAG)
        }
    }

    private fun backNavigation() {
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

    private fun backNavigationToHome() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                householdRegistrationViewModel.setUserJourney(AnalyticsDefinedParams.ONHOMEBUTTONTRIGGERED)
                startActivityWithoutSplashScreen()
            }
        }
    }

    private fun onBackPressPopStack() {
        this@AddNewMemberActivity.finish()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnNext.id -> {
                withNetworkCheck(
                    connectivityManager,
                    onNetworkAvailable = { addNewMemberCreate(v) })
            }
        }
    }

    private fun addNewMemberCreate(v: View) {
        val memberRegistrationFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as MemberRegistrationFragment
        memberRegistrationFragment.medicalReviewAddMember(v)
    }

    override fun onDialogDismissListener(isFinish: Boolean) {
        startActivityWithoutSplashScreen()
    }
    private fun getCurrentLocation() {
        val locationManager = SpiceLocationManager(this)
        locationManager.getCurrentLocation {
            householdRegistrationViewModel.setCurrentLocation(it)
        }
    }

    override fun onResume() {
        super.onResume()
        getCurrentLocation()
    }

    private fun validateFormInputs(): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (fragment is MemberRegistrationFragment){
            return fragment.getEnteredInputs()
        }
        return false
    }
}