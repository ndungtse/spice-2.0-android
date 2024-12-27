package com.medtroniclabs.spice.ui.landing

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.medtroniclabs.spice.R
import com.medtroniclabs.spice.appextensions.gone
import com.medtroniclabs.spice.appextensions.textOrHyphen
import com.medtroniclabs.spice.appextensions.visible
import com.medtroniclabs.spice.common.CommonUtils.getContactNumber
import com.medtroniclabs.spice.data.UserProfile
import com.medtroniclabs.spice.databinding.FragmentProfileDialogBinding
import com.medtroniclabs.spice.formgeneration.extension.safeClickListener
import com.medtroniclabs.spice.network.resource.ResourceState
import com.medtroniclabs.spice.ui.BaseActivity
import com.medtroniclabs.spice.ui.landing.viewmodel.LandingViewModel

class ProfileDialogFragment : DialogFragment(), View.OnClickListener {

    private lateinit var binding: FragmentProfileDialogBinding
    private val viewModel: LandingViewModel by activityViewModels()

    companion object {
        const val TAG = "ProfileDialogFragment"
        fun newInstance(): ProfileDialogFragment {
            return ProfileDialogFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        attachObservers()
        getData()
    }

    private fun getData() {
        with(viewModel) {
            setUserJourney(getString(R.string.profile))
            getUserProfile()
        }
    }

    private fun attachObservers() {
        viewModel.userProfileLiveData.observe(this) { resourceState ->
            when (resourceState.state) {
                ResourceState.LOADING -> {
                    (activity as? BaseActivity)?.showLoading()
                }

                ResourceState.SUCCESS -> {
                    (activity as? BaseActivity)?.hideLoading()
                    resourceState.data?.let {
                        setUserProfileData(it)
                    }
                }

                ResourceState.ERROR -> {
                    (activity as? BaseActivity)?.hideLoading()
                }
            }
        }
    }

    private fun initView() {
        with(binding) {
            ivClose.safeClickListener(this@ProfileDialogFragment)
        }
    }

    private fun setUserProfileData(user: UserProfile) {
        with(binding) {
            tvName.text = if (!user.firstName.isNullOrBlank() && !user.lastName.isNullOrBlank()) {
                requireContext().getString(
                    R.string.firstname_lastname,
                    user.firstName,
                    user.lastName
                )
            } else {
                getString(R.string.separator_double_hyphen)
            }
            tvGenderText.text = user.gender.takeIf { it?.isNotBlank() == true } ?: getString(R.string.separator_double_hyphen)
            tvDesignationText.text = user.designation?.name.textOrHyphen()
            tvEmailText.text = user.username.takeIf { it.isNotBlank() } ?: getString(R.string.separator_double_hyphen)
            tvPhoneNumberText.text = getContactNumber(user.phoneNumber.takeIf { it?.isNotBlank() == true }) ?: getString(R.string.separator_double_hyphen)
            if(user.villages.isNullOrEmpty()) {
                villageGroup.gone()
                tvVillageText.text = getString(R.string.hyphen_symbol)
            } else {
                villageGroup.visible()
                tvVillageText.text = user.villages.joinToString(separator = ", ") { it.name }
            }
            tvAssignedHealthFacilityText.text = if(user.organizations.isNullOrEmpty()) getString(R.string.hyphen_symbol) else user.organizations.joinToString(separator = ", ") { it.name }
                ?: getString(R.string.hyphen_symbol)
            tvSuiteAccessText.text = user.suiteAccess?.let {
                user.suiteAccess[0]
            } ?: getString(R.string.separator_double_hyphen)
            val displayNames = user.roles
                .mapNotNull { it.displayName }
                .filter { it.isNotBlank() }

            tvRoleText.text = if (displayNames.isEmpty()) {
                getString(R.string.separator_double_hyphen)
            } else {
                displayNames.joinToString()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        handleDialogHeight()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleDialogHeight()
    }

    private fun handleDialogHeight() {
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivClose -> {
                dismiss()
            }

            R.id.btnCancel -> {
                dismiss()
            }
        }
    }
}