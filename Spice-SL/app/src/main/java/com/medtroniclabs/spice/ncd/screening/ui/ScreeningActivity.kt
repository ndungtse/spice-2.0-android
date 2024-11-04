package com.medtroniclabs.spice.ncd.screening.ui

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityScreeningBinding
import com.medtroniclabs.spice.ncd.screening.fragment.GeneralDetailsFragment
import com.medtroniclabs.spice.ncd.screening.fragment.ScreeningFormBuilderFragment
import com.medtroniclabs.spice.ncd.screening.fragment.ScreeningSummaryFragment
import com.medtroniclabs.spice.ncd.screening.fragment.StatsFragment
import com.medtroniclabs.spice.ncd.screening.viewmodel.ScreeningFormBuilderViewModel
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ncd.registration.fragment.TermsAndConditionsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScreeningActivity : BaseActivity() {

    private lateinit var binding: ActivityScreeningBinding
    private val viewModel: ScreeningFormBuilderViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding = ActivityScreeningBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.screening),
            homeAndBackVisibility = Pair(true, true),
            callback = {
                backNavigation()
            }
        )
        initView()
    }

    private fun initView() {
        replaceFragmentIfExists<GeneralDetailsFragment>(
            R.id.screeningParentLayout,
            bundle = null,
            tag = GeneralDetailsFragment.TAG
        )
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            backNavigation()
        }
    }

    fun backNavigation() {
        val fragmentManager = supportFragmentManager

        when (fragmentManager.findFragmentById(R.id.screeningParentLayout)) {
            is GeneralDetailsFragment, is ScreeningSummaryFragment -> startActivityWithoutSplashScreen()
            is StatsFragment -> {
                if (viewModel.screeningSaveResponse.value?.data != null) {
                    startActivityWithoutSplashScreen()
                } else {
                    replaceFragment<GeneralDetailsFragment>(GeneralDetailsFragment.TAG)
                }
            }

            is TermsAndConditionsFragment -> replaceFragment<StatsFragment>(StatsFragment.TAG)
            is ScreeningFormBuilderFragment -> replaceFragment<TermsAndConditionsFragment>(
                TermsAndConditionsFragment.TAG
            )

            else -> {

            }
        }
    }

    private inline fun <reified T : Fragment> replaceFragment(tag: String) {
        replaceFragmentIfExists<T>(
            R.id.screeningParentLayout,
            bundle = null,
            tag = tag
        )
    }

}