package org.medtroniclabs.uhis.ui.medicalreview.addnewmember

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.app.analytics.utils.AnalyticsDefinedParams
import org.medtroniclabs.uhis.appextensions.invisible
import org.medtroniclabs.uhis.common.SpiceLocationManager
import org.medtroniclabs.uhis.databinding.ActivityAddNewMemberBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.dialog.SuccessDialogFragment
import org.medtroniclabs.uhis.ui.household.viewmodel.HouseRegistrationViewModel
import org.medtroniclabs.uhis.ui.landing.OnDialogDismissListener
import org.medtroniclabs.uhis.ui.medicalreview.utils.MedicalReviewDefinedParams
import org.medtroniclabs.uhis.ui.member.MemberRegistrationFragment

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
                if (validateFormInputs()) {
                    backNavigation()
                } else {
                    onBackPressPopStack()
                }
            },
            callbackHome = {
                backNavigationToHome()
            },
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
            tag = MemberRegistrationFragment::class.simpleName,
        )
        supportFragmentManager.setFragmentResultListener(
            MedicalReviewDefinedParams.MEMBER_REG,
            this,
        ) { _, _ ->
            SuccessDialogFragment.newInstance(isMember = true).show(supportFragmentManager, SuccessDialogFragment.TAG)
        }
    }

    private fun backNavigation() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true,
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
            isNegativeButtonNeed = true,
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
                    onNetworkAvailable = { addNewMemberCreate(v) },
                )
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
        if (fragment is MemberRegistrationFragment) {
            return fragment.getEnteredInputs()
        }
        return false
    }
}
