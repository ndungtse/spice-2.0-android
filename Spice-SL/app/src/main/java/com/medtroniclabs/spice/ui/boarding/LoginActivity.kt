package com.medtroniclabs.spice.ui.boarding

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.hideKeyboard
import com.medtroniclabs.spice.appextensions.invisible
import com.medtroniclabs.spice.common.CommonUtils
import com.medtroniclabs.spice.common.DefinedParams
import com.medtroniclabs.spice.common.EncryptionUtil
import com.medtroniclabs.spice.common.RegexConstants.Contains_Number
import com.medtroniclabs.spice.common.SecuredPreference
import com.medtroniclabs.spice.common.Validator
import com.medtroniclabs.spice.databinding.ActivityLoginBinding
import com.medtroniclabs.spice.formgeneration.extension.markMandatory
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.boarding.viewmodel.LoginViewModel
import com.medtroniclabs.spice.ui.landing.LandingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private var unSyncedDataCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setMainContentView(binding.root)
        initView()
        setListeners()
        attachObservers()
        checkNotificationPermission()
    }

    private fun attachObservers() {
        viewModel.unSyncedDataCountLiveData.observe(this) {
            unSyncedDataCount = it
        }
        viewModel.loginResponseLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    showLoading()
                }

                ResourceState.SUCCESS -> {
                    hideLoading()
                    resourceState?.data?.let {
                        SecuredPreference.putString(
                            SecuredPreference.EnvironmentKey.USERNAME.name, it.username
                        )
                        SecuredPreference.putString(
                            SecuredPreference.EnvironmentKey.PHONE_NUMBER.name, it.phoneNumber
                        )
                        triggerResourceLoading()
                    }
                }

                ResourceState.ERROR -> {
                    hideLoading()
                    resourceState.message?.let {
                        showErrorSnackBar(it)
                    }
                }
            }
        }

    }

    private fun triggerResourceLoading() {
        startAsNewActivity(
            Intent(
                this@LoginActivity, ResourceLoadingScreen::class.java
            )
        )
    }

    private fun handleOfflineLoginSuccess() {
        SecuredPreference.putBoolean(
            SecuredPreference.EnvironmentKey.ISOFFLINELOGIN.name, true
        )
        startAsNewActivity(
            Intent(
                this@LoginActivity, LandingActivity::class.java
            )
        )
    }

    private fun setListeners() {
        binding.btnLogin.safeClickListener(this)
        binding.tvForgotPassword.safeClickListener(this)
    }

    private fun initView() {
        binding.tvUserNameLabel.markMandatory()
        binding.tvPasswordLabel.markMandatory()
        binding.tvForgotPassword.invisible()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnLogin -> {
                hideKeyboard(view)
                validateLoginInputs()
            }

            R.id.tvForgotPassword -> {

            }
        }
    }

    private fun validateLoginInputs() {
        val userName = binding.userName.text.toString().trim()
        val password = binding.password.text.toString().trim()

        //Username blank check
        if (userName.isBlank()) {
            binding.tvUserEmailError.visibility = View.VISIBLE
            binding.tvUserEmailError.text = getString(R.string.email_cannot_be_empty)
            return
        }

        //Password blank check
        if (password.isBlank()) {
            binding.tvUserEmailError.visibility = View.GONE
            binding.tvUserPasswordError.visibility = View.VISIBLE
            binding.tvUserPasswordError.text = getString(R.string.password_cannot_be_empty)
            return
        }

        //Validate the username is phone number or email
        if (userName.contains(DefinedParams.AT_CHAR)) {
            if (!Validator.isEmailValid(userName)) {
                binding.tvUserEmailError.visibility = View.VISIBLE
                binding.tvUserEmailError.text = getString(R.string.email_phone_invalid)
                return
            }
        } else {
            if (!(Validator.isValidMobileNumber(userName))) {
                binding.tvUserEmailError.visibility = View.VISIBLE
                binding.tvUserEmailError.text = getString(R.string.email_phone_invalid)
                return
            }
        }

        // Check network connection and offline login
        binding.tvUserEmailError.visibility = View.GONE
        binding.tvUserPasswordError.visibility = View.GONE
        if (!connectivityManager.isNetworkAvailable()) {
            showErrorSnackBar(getString(R.string.no_internet_error))
            val isToShowAlert = ((userName == SecuredPreference.getString(
                SecuredPreference.EnvironmentKey.USERNAME.name
            ) || userName == SecuredPreference.getString(
                SecuredPreference.EnvironmentKey.PHONE_NUMBER.name
            )) && EncryptionUtil.getSecurePassword(
                password
            ) == SecuredPreference.getString(
                SecuredPreference.EnvironmentKey.PASSWORD.name
            ))
            if (isToShowAlert && CommonUtils.isChw()) {
                showErrorDialogue(
                    getString(R.string.alert),
                    message = getString(R.string.offline_login_message),
                    isNegativeButtonNeed = true
                ) { buttonState ->
                    if (buttonState) {
                        handleOfflineLoginSuccess()
                    }
                }
            }

            return
        }

        // Check different account login
        val oldUserName =
            SecuredPreference.getString(SecuredPreference.EnvironmentKey.USERNAME.name)
        val oldPhoneNumber =
            SecuredPreference.getString(SecuredPreference.EnvironmentKey.PHONE_NUMBER.name)
        val isNumber = userName.matches(Regex(Contains_Number))
        if (oldUserName != null && validateNameOrNumber(
                isNumber, oldPhoneNumber, oldUserName, userName
            )
        ) {
            if (unSyncedDataCount > 0) {
                showErrorDialogue(
                    title = getString(R.string.warning_different_login_title),
                    message = getString(R.string.warning_different_login_message, oldUserName),
                    positiveButtonName = getString(R.string.okay),
                    okayBtnEnable = true,
                ) {

                }
            } else { // Different user login so clear last synced at
                SecuredPreference.remove(SecuredPreference.EnvironmentKey.SERVER_LAST_SYNCED.name)
                viewModel.doLogin(userName, password)
            }
        } else {
            // Same user login in online
            viewModel.doLogin(userName, password)
        }
    }

    private fun validateNameOrNumber(
        isNumber: Boolean, oldPhoneNumber: String?, oldUserName: String, userName: String
    ): Boolean {
        return if (isNumber) {
            oldPhoneNumber != userName
        } else {
            oldUserName != userName
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT > 32) {
            if (!shouldShowRequestPermissionRationale("")){
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                )
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { _ ->

        }
}