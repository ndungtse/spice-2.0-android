package org.medtroniclabs.uhis.ui.landing

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import org.medtroniclabs.uhis.R
import org.medtroniclabs.uhis.appextensions.gone
import org.medtroniclabs.uhis.appextensions.textOrEmpty
import org.medtroniclabs.uhis.appextensions.textOrHyphen
import org.medtroniclabs.uhis.appextensions.visible
import org.medtroniclabs.uhis.common.CommonUtils
import org.medtroniclabs.uhis.common.CommonUtils.getContactNumber
import org.medtroniclabs.uhis.data.UserProfile
import org.medtroniclabs.uhis.databinding.FragmentProfileDialogBinding
import org.medtroniclabs.uhis.formgeneration.extension.safeClickListener
import org.medtroniclabs.uhis.network.resource.ResourceState
import org.medtroniclabs.uhis.ui.BaseActivity
import org.medtroniclabs.uhis.ui.landing.viewmodel.LandingViewModel

class ProfileDialogFragment : DialogFragment(), View.OnClickListener {
    private lateinit var binding: FragmentProfileDialogBinding
    private val viewModel: LandingViewModel by activityViewModels()

    companion object {
        const val TAG = "ProfileDialogFragment"

        fun newInstance(): ProfileDialogFragment = ProfileDialogFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProfileDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
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
        if (CommonUtils.isNonCommunity()) {
            binding.tvDesignationText.visible()
            binding.tvDesignationLabel.visible()
            binding.tvDesignationSeparator.visible()
        } else {
            binding.tvDesignationText.gone()
            binding.tvDesignationLabel.gone()
            binding.tvDesignationSeparator.gone()
        }
        with(binding) {
            ivClose.safeClickListener(this@ProfileDialogFragment)
        }
    }

    private fun setUserProfileData(user: UserProfile) {
        with(binding) {
            tvName.text = requireContext().getString(
                R.string.firstname_lastname,
                user.firstName.textOrEmpty(),
                user.lastName.textOrEmpty(),
            )
            tvGenderText.text = user.gender.takeIf { it?.isNotBlank() == true } ?: getString(R.string.separator_double_hyphen)
            tvDesignationText.text = user.designation?.name.textOrHyphen()
            tvEmailText.text = user.username.takeIf { it.isNotBlank() } ?: getString(R.string.separator_double_hyphen)
            tvPhoneNumberText.text = getContactNumber(user.phoneNumber.takeIf { it?.isNotBlank() == true }) ?: getString(R.string.separator_double_hyphen)
            if (user.villages.isNullOrEmpty()) {
                villageGroup.gone()
                tvVillageText.text = getString(R.string.separator_double_hyphen)
            } else {
                villageGroup.visible()
                tvVillageText.text = user.villages.joinToString(separator = ", ") { it.name }
            }
            tvAssignedHealthFacilityText.text =
                if (user.organizations.isNullOrEmpty()) {
                    getString(R.string.hyphen_symbol)
                } else {
                    user.organizations.joinToString(separator = ", ") { it.name }
                        ?: getString(R.string.hyphen_symbol)
                }
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
            WindowManager.LayoutParams.WRAP_CONTENT,
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
