package com.medtroniclabs.spice.ui.home

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.app.analytics.utils.AnalyticsDefinedParams
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityToolsBinding
import com.medtroniclabs.spice.ncd.medicalreview.NCDMRUtil.EncounterReference
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.MenuConstants
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
                toolsViewModel.setUserJourney(AnalyticsDefinedParams.ONHOMEBUTTONTRIGGERED)
                val intent = Intent(this, LandingActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                finish()
            }
        )
        initializeView()
    }

    override fun onResume() {
        super.onResume()
        toolsViewModel.setUserJourney(AnalyticsDefinedParams.ASSESSMENTTOOLSELECTION)
    }

    private fun initializeView() {
        toolsViewModel.selectedHouseholdMemberID = intent.getLongExtra(DefinedParams.MemberID, -1)
        toolsViewModel.selectedMemberDob = intent.getStringExtra(DefinedParams.DOB)
        toolsViewModel.followUpId = intent.getLongExtra(DefinedParams.FollowUpId, -1)
        val isDeepLink = intent.getBooleanExtra(DefinedParams.IsDeepLink, false)
        val bundle = Bundle()
        bundle.putString(DefinedParams.FhirId, intent.getStringExtra(DefinedParams.FhirId))
        bundle.putString(DefinedParams.ORIGIN, intent.getStringExtra(DefinedParams.ORIGIN))
        bundle.putString(DefinedParams.Gender, intent.getStringExtra(DefinedParams.Gender))
        bundle.putBoolean(MenuConstants.FOLLOW_UP, intent.getBooleanExtra(MenuConstants.FOLLOW_UP,false))
        bundle.putBoolean(DefinedParams.IsDeepLink,isDeepLink)
        // only for Africa encounterReference is visit id(Support landscape )
        intent.getStringExtra(EncounterReference)?.let { encounterReference ->
            bundle.putString(EncounterReference, encounterReference)
            requestedOrientation = if (encounterReference.isNotBlank()) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        } ?: run {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        val fragmentTag =
            ToolsMenuFragment.TAG
        var fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        if (fragment == null) {
            fragment = ToolsMenuFragment.newInstance()
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .add(R.id.menuItemsFragment, fragment, fragmentTag)
                .commit()
        }
    }
}