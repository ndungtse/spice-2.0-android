package com.medtroniclabs.spice.ui.boarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.triggerOneTimeWorker
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.RoleConstant
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.databinding.ActivityResourceLoadingScreenBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.ncd.landing.ui.UserTermsConditionsActivity
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.boarding.viewmodel.ResourceLoadingViewModel
import com.medtroniclabs.spice.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResourceLoadingScreen : BaseActivity() {
    private lateinit var binding: ActivityResourceLoadingScreenBinding
    private val viewModel: ResourceLoadingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResourceLoadingScreenBinding.inflate(layoutInflater)
        setMainContentView(binding.root)
        initView()
        attachObserver()
        setViewListener()
    }

    private fun initView() {
        viewModel.changeFacility = intent.getBooleanExtra(DefinedParams.changeFacility,false)
        getMetaData()
    }

    private fun attachObserver() {
        viewModel.deviceDetailsLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                }

                ResourceState.SUCCESS -> {
                    viewModel.getMetaDataInformation()
                }

                ResourceState.ERROR -> {
                    handleError()
                }
            }
        }

        viewModel.metaDataCompleteLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.tvOfflineSyncMessage.gone()
                }

                ResourceState.SUCCESS -> {
                    val userRole = SecuredPreference.getUserDetails()?.roles?.joinToString { it.name }
                    if (userRole != null) {
                        if (userRole.contains(RoleConstant.COMMUNITY_HEALTH_WORKER) && CommonUtils.isCommunity()) {
                            viewModel.downloadInitialDetails()
                        } else if (CommonUtils.isNonCommunity() && CommonUtils.isChp()) {
                            viewModel.downloadTheFollowUpData()
                        } else {
                            launchLandingScreen()
                        }
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
                    binding.tvOfflineSyncMessage.gone()
                }

                ResourceState.SUCCESS -> {
                    launchLandingScreen()
                    if (CommonUtils.isNonCommunity() && connectivityManager.isNetworkAvailable()) {
                        this.triggerOneTimeWorker()
                        viewModel.downloadTheFollowUpData()
                    }
                }

                ResourceState.ERROR -> {
                    handleError()
                }
            }
        }

        viewModel.ncdFollowUpLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    binding.tvOfflineSyncMessage.gone()
                }

                ResourceState.SUCCESS -> {
                    launchLandingScreen()
                    if (CommonUtils.isNonCommunity() && connectivityManager.isNetworkAvailable()) {
                        this.triggerOneTimeWorker()
                    }
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
            getMetaData()
        }
    }

    private fun getMetaData() {
        if (viewModel.changeFacility)
            viewModel.updateDeviceDetails(this)
        else
            viewModel.getMetaDataInformation()
    }

    private fun launchLandingScreen() {
        SecuredPreference.putBoolean(
            SecuredPreference.EnvironmentKey.ISLOGGEDIN.name,
            true
        )
        SecuredPreference.putBoolean(
            SecuredPreference.EnvironmentKey.ISMETALOADED.name,
            true
        )
        if (CommonUtils.isNonCommunity() && !SecuredPreference.getTermsAndConditionsStatus())
            startActivity(Intent(this, UserTermsConditionsActivity::class.java))
        else
            startActivity(Intent(this, LandingActivity::class.java))
        finish()
    }

}