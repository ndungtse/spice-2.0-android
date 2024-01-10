package com.medtroniclabs.spice.ui.home

import android.os.Bundle
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityToolsBinding
import com.medtroniclabs.spice.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ToolsActivity : BaseActivity() {

    private lateinit var binding: ActivityToolsBinding

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
        supportFragmentManager.beginTransaction()
            .add(R.id.menuItemsFragment, ToolsMenuFragment())
            .commit()
    }
}