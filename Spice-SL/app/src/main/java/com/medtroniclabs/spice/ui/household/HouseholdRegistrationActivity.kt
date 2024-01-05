package com.medtroniclabs.spice.ui.household

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.databinding.ActivityHouseholdRegistrationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HouseholdRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHouseholdRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}