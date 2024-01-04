package com.medtroniclabs.spice.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.medtroniclabs.spice.sl.R
import com.medtroniclabs.spice.sl.databinding.ActivityBaseBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}