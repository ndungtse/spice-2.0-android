package org.medtroniclabs.uhis.ui.dashboard.ncd

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.databinding.ActivityNcdDashboardVeiwBinding
import org.medtroniclabs.uhis.ui.BaseActivity

@AndroidEntryPoint
class NCDDashboardViewActivity : BaseActivity() {
    private lateinit var binding: ActivityNcdDashboardVeiwBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNcdDashboardVeiwBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.dashboard),
            homeAndBackVisibility = Pair(false, true),
        )
        loadDashboardFragment()
    }

    override fun consumeImeInsets() = true

    private fun loadDashboardFragment() {
        replaceFragmentInId<DashboardFragment>(
            R.id.fragmentContainerView,
            bundle = intent.extras,
            tag = DashboardFragment.TAG,
        )
    }
}
