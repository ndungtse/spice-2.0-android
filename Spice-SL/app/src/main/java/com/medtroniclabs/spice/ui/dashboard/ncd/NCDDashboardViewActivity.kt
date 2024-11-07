package com.medtroniclabs.spice.ui.dashboard.ncd

import android.os.Bundle
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityNcdDashboardVeiwBinding
import com.medtroniclabs.spice.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NCDDashboardViewActivity : BaseActivity() {

    private lateinit var binding: ActivityNcdDashboardVeiwBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdDashboardVeiwBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root, isToolbarVisible = true, title = getString(R.string.dashboard),homeAndBackVisibility = Pair(false, true)
        )
        loadSearchFragment()
    }

    private fun loadSearchFragment() {
            replaceFragmentInId<DashboardFragment>(
                R.id.fragmentContainerView,
                bundle = intent.extras,
                tag = DashboardFragment.TAG
            )
    }
}