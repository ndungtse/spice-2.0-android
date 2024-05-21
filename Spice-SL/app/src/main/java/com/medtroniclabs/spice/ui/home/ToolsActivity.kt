package com.medtroniclabs.spice.ui.home

import android.os.Bundle
import androidx.activity.viewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.databinding.ActivityToolsBinding
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.mypatients.fragment.PatientMenuFragment
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
            setTitle(intent.getStringExtra(DefinedParams.MenuTitle) ?: getString(R.string.search_patient))
            PatientMenuFragment.newInstance(intent.getStringExtra(DefinedParams.PatientId),intent.getStringExtra(DefinedParams.ID),intent.getStringExtra(DefinedParams.Gender))
        } else {
            ToolsMenuFragment.newInstance()
        }
        supportFragmentManager.beginTransaction()
            .add(R.id.menuItemsFragment, fragment)
            .commit()
    }
}