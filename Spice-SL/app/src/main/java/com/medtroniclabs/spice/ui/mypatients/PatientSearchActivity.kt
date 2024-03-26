package com.medtroniclabs.spice.ui.mypatients

import android.os.Bundle
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityPatientSearchBinding
import com.medtroniclabs.spice.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PatientSearchActivity : BaseActivity() {

    private lateinit var binding: ActivityPatientSearchBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientSearchBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            title = getString(R.string.search_patient),
            isToolbarVisible = true,
            homeAndBackVisibility = Pair(true, true)
        )
    }
}