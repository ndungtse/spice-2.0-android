package com.medtroniclabs.spice.ui.household

import android.os.Bundle
import com.medtroniclabs.spice.databinding.ActivityHouseholdSearchBinding
import com.medtroniclabs.spice.ui.BaseActivity

class HouseholdSearchActivity : BaseActivity() {

    private lateinit var binding: ActivityHouseholdSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}