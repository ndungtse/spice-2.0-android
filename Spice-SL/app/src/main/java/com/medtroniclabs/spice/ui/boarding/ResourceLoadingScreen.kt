package com.medtroniclabs.spice.ui.boarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityResourceLoadingScreenBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.boarding.viewmodel.ResourceLoadingViewModel
import com.medtroniclabs.spice.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResourceLoadingScreen : BaseActivity() {
    private lateinit var binding: ActivityResourceLoadingScreenBinding
    private val viewModel: ResourceLoadingViewModel by viewModels()
    private val ROLE_CHW = "CHW"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResourceLoadingScreenBinding.inflate(layoutInflater)
        setMainContentView(binding.root)
        attachObserver()
        setViewListener()
    }

    private fun attachObserver() {
        viewModel.metaDataCompleteLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.tvOfflineSyncMessage.gone()
                }

                ResourceState.SUCCESS -> {
                    val userRole = SecuredPreference.getUserDetails().roles.joinToString { it.name }
                    if (userRole == ROLE_CHW) {
                        viewModel.downloadInitialDetails()
                    } else {
                        SecuredPreference.putBoolean(
                            SecuredPreference.EnvironmentKey.ISMETALOADED.name,
                            true
                        )
                        startActivity(Intent(this, LandingActivity::class.java))
                        finish()
                    }
                }
                ResourceState.ERROR -> {
                    handleError()
                }
            }
        }

        viewModel.householdsLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.tvOfflineSyncMessage.visible()
                }

                ResourceState.SUCCESS -> {
                    startActivity(Intent(this, LandingActivity::class.java))
                    finish()
                }

                ResourceState.ERROR -> {
                    handleError()
                }
            }
        }
    }


    private fun handleError() {
        SecuredPreference.putBoolean(
            SecuredPreference.EnvironmentKey.ISMETALOADED.name,
            false
        )
        SecuredPreference.putBoolean(
            SecuredPreference.EnvironmentKey.ISLOGGEDIN.name,
            false
        )
        binding.actionButton.visibility = View.VISIBLE
    }

    private fun setViewListener() {
        binding.actionButton.safeClickListener {
            binding.actionButton.visibility = View.GONE
            viewModel.getMetaDataInformation()
        }
    }

}