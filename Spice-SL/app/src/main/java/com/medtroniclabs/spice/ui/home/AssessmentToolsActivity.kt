package com.medtroniclabs.spice.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityToolsBinding
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentToolsActivity : BaseActivity() {

    private lateinit var binding: ActivityToolsBinding

    private val toolsViewModel: ToolsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToolsBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.assessment),
            homeAndBackVisibility = Pair(true, true),
            callbackHome = {
                val intent = Intent(this, LandingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            }
        )
        initializeView()
    }

    private fun initializeView() {
        toolsViewModel.selectedHouseholdMemberID = intent.getLongExtra(DefinedParams.MemberID, -1)
        toolsViewModel.followUpId = intent.getLongExtra(DefinedParams.FollowUpId, -1)
        val fragmentTag =
            ToolsMenuFragment.TAG
        var fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        if (fragment == null) {
            fragment = ToolsMenuFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .add(R.id.menuItemsFragment, fragment, fragmentTag)
                .commit()
        }
    }
}