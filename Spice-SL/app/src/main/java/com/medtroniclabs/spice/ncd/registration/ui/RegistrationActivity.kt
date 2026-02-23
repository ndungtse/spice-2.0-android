package com.medtroniclabs.spice.ncd.registration.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityRegistrationBinding
import com.medtroniclabs.spice.ncd.registration.fragment.RegistrationFormFragment
import com.medtroniclabs.spice.ncd.registration.fragment.RegistrationSummaryFragment
import com.medtroniclabs.spice.ncd.registration.fragment.TermsAndConditionsFragment
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.mypatients.viewmodel.PatientDetailViewModel

class RegistrationActivity : BaseActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private val patientDetailViewModel: PatientDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fixOrientation()
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.registration),
            homeAndBackVisibility = Pair(true, true),
            callback = {
                backNavigation()
            },
            callbackHome = {
                backNavigation()
            },
        )
        attachObserver()
        getPatientDetails()
    }

    private fun onBackPressPopStack() {
        this@RegistrationActivity.finish()
    }

    private fun attachObserver() {
        patientDetailViewModel.patientDetailsLiveData.observe(this) { resource ->
            handleResourceState(
                resource,
                onSuccess = { loadTermsAndConditionsFragment() },
                onBackPressPopStack = ::onBackPressPopStack,
            )
        }
    }

    private fun fixOrientation() {
        requestedOrientation =
            if (CommonUtils.checkIsTablet(this)) ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun getPatientDetails() {
        patientDetailViewModel.origin = intent?.getStringExtra(DefinedParams.ORIGIN)
        intent.getStringExtra(DefinedParams.FhirId)?.let { id ->
            patientDetailViewModel.getPatients(
                id,
                origin = patientDetailViewModel.origin?.lowercase(),
            )
        } ?: run {
            loadTermsAndConditionsFragment()
        }
    }

    private fun loadTermsAndConditionsFragment() {
        setTitle(getString(R.string.terms_and_conditions))
        val bundle = Bundle().apply {
            putString(TermsAndConditionsFragment.FORM_TYPE, DefinedParams.Registration)
        }
        replaceFragmentInId<TermsAndConditionsFragment>(
            binding.formsFragmentContainer.id,
            bundle = bundle,
            tag = TermsAndConditionsFragment.TAG,
        )
    }

    fun loadRegistrationFormFragment(bundle: Bundle) {
        setTitle(getString(R.string.registration))
        replaceFragmentInId<RegistrationFormFragment>(
            binding.formsFragmentContainer.id,
            bundle = bundle,
            tag = RegistrationFormFragment.TAG,
        )
    }

    fun loadRegistrationSummaryFragment() {
        setTitle(getString(R.string.registration))
        replaceFragmentInId<RegistrationSummaryFragment>(
            binding.formsFragmentContainer.id,
            tag = RegistrationSummaryFragment.TAG,
        )
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backNavigation()
            }
        }

    private fun backNavigation() {
        showErrorDialogue(
            getString(R.string.alert),
            getString(R.string.exit_reason),
            isNegativeButtonNeed = true,
        ) { isPositive ->
            if (isPositive) {
                startActivityWithoutSplashScreen()
            }
        }
    }
}
