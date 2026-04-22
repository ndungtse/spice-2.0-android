package org.medtroniclabs.uhis.ui.common

import android.os.Bundle
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.databinding.ActivityPatientSearchBinding
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.mypatients.fragment.PatientSearchFragment

class PatientSearchActivity : BaseActivity() {
    private lateinit var binding: ActivityPatientSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPatientSearchBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.search_patient),
        )

        loadSearchFragment()
    }

    override fun consumeImeInsets() = true

    private fun loadSearchFragment() {
        replaceFragmentInId<PatientSearchFragment>(
            R.id.fragmentContainerView,
            bundle = intent.extras,
            tag = PatientSearchFragment.TAG,
        )
    }
}
