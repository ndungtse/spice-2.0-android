package com.medtroniclabs.spice.ui.home

import android.os.Bundle
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityToolsBinding
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.medicalreview.PatientMenuFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ToolsActivity : BaseActivity() {

    private lateinit var binding: ActivityToolsBinding

    private val toolsViewModel : ToolsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToolsBinding.inflate(layoutInflater)
        setMainContentView(
            binding.root,
            isToolbarVisible = true,
            title = getString(R.string.assessment)
        )
        initializeView()
    }

    private fun initializeView() {
        toolsViewModel.selectedHouseholdMemberID = intent.getLongExtra(DefinedParams.MemberID, -1)
        val fragment = if (toolsViewModel.selectedHouseholdMemberID == -1L) {
            PatientMenuFragment.newInstance()
        } else {
            ToolsMenuFragment.newInstance()
        }
        supportFragmentManager.beginTransaction()
            .add(R.id.menuItemsFragment, fragment)
            .commit()
    }
}