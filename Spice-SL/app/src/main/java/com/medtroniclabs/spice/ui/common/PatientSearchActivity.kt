package com.medtroniclabs.spice.ui.common

import android.os.Bundle
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityPatientSearchBinding
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientSearchFragment

class PatientSearchActivity : BaseActivity() {

    private lateinit var binding: ActivityPatientSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPatientSearchBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root, isToolbarVisible = true, title = getString(R.string.search_patient)
        )

        loadSearchFragment()
    }

    private fun loadSearchFragment() {
        replaceFragmentInId<PatientSearchFragment>(
            R.id.fragmentContainerView,
            bundle = intent.extras,
            tag = PatientSearchFragment.TAG
        )
    }
}