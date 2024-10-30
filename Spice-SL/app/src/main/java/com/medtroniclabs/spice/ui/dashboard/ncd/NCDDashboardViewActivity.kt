package com.medtroniclabs.spice.ui.dashboard.ncd

import android.os.Bundle
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.databinding.ActivityNcdDashboardVeiwBinding
import com.medtroniclabs.spice.ui.BaseActivity

class NCDDashboardViewActivity : BaseActivity() {

    private lateinit var binding: ActivityNcdDashboardVeiwBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdDashboardVeiwBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root, isToolbarVisible = true, title = getString(R.string.dashboard)
        )
        loadSearchFragment()
    }

    private fun loadSearchFragment() {
        if (CommonUtils.checkIsTablet(this)) {
            replaceFragmentInId<NCDDashboardTabFragment>(
                R.id.fragmentContainerView,
                bundle = intent.extras,
                tag = NCDDashboardTabFragment.TAG
            )
        } else {
            replaceFragmentInId<DashboardFragment>(
                R.id.fragmentContainerView,
                bundle = intent.extras,
                tag = DashboardFragment.TAG
            )
        }
    }
}