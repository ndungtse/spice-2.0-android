package com.medtroniclabs.spice.ui.registration

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityRegistrationBinding
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.registration.fragment.RegistrationFormFragment
import com.medtroniclabs.spice.ui.registration.fragment.RegistrationSummaryFragment
import com.medtroniclabs.spice.ui.registration.fragment.TermsAndConditionsFragment

class RegistrationActivity : BaseActivity() {
    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            }
        )
        loadTermsAndConditionsFragment()
    }

    private fun loadTermsAndConditionsFragment() {
        setTitle(getString(R.string.terms_and_conditions))
        val bundle = Bundle().apply {
            putString(TermsAndConditionsFragment.FORM_TYPE, DefinedParams.Registration)
        }
        replaceFragmentInId<TermsAndConditionsFragment>(
            binding.formsFragmentContainer.id,
            bundle = bundle,
            tag = TermsAndConditionsFragment.TAG
        )
    }

    fun loadRegistrationFormFragment() {
        setTitle(getString(R.string.registration))
        replaceFragmentInId<RegistrationFormFragment>(
            binding.formsFragmentContainer.id,
            tag = RegistrationFormFragment.TAG
        )
    }

    fun loadRegistrationSummaryFragment() {
        setTitle(getString(R.string.registration))
        replaceFragmentInId<RegistrationSummaryFragment>(
            binding.formsFragmentContainer.id,
            tag = RegistrationSummaryFragment.TAG
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
            isNegativeButtonNeed = true
        ) { isPositive ->
            if (isPositive) {
                startActivityWithoutSplashScreen()
            }
        }
    }
}