package com.medtroniclabs.spice.ncd.screening

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityScreeningBinding
import com.medtroniclabs.spice.ncd.screening.fragment.GeneralDetailsFragment
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientSearchFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScreeningActivity : BaseActivity() {

    private lateinit var binding: ActivityScreeningBinding

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

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        }
}